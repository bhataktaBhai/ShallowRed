package chess;

import except.NoKingException;
import except.NullPieceException;
import pieces.King;
import pieces.Pawn;
import pieces.Piece;
import java.util.ArrayList;
import java.util.Arrays;

public class Engine2
{
    private ArrayList<MoveData> tree;
    private final int LAYER = 4;
    
        class MoveData
        {
            private final Move MOVE;
            private ArrayList<MoveData> TREE;
            private final float EVAL;
            
            MoveData(Piece p, int[] move, float evaluation)
            {
                MOVE = new Move(p, move);
                EVAL = evaluation;
            }
            
            void setTree(ArrayList<MoveData> tree) {
                TREE = tree;
            }
            
            @Override
            public String toString()
            {
                return MOVE.toString();
            }
            public String toString(Piece[][] board)
            {
                return MOVE.toString(board);
            }
        }
    
    public Move play(Position pos, Move move) throws NullPieceException, NoKingException, Exception
    {
        //System.out.println("Getting tree...");
        tree = getTree(move);
        //System.out.println("Growing tree...");
        tree = growTree(tree, pos, LAYER);
        //System.out.println("Tree grown.");
        //printTree(tree, 0);
        double bestMove = Integer.MIN_VALUE + 1;
        int index = 0; int n = 0;
        for(int i = 1; i < tree.size(); i++) {
            MoveData possibleMove = tree.get(i);
            double betterMove;
            if(possibleMove.TREE == null) {
                System.out.println("Excuse me?");
                betterMove = possibleMove.EVAL;
            }
            else {
                betterMove = - bestMove(possibleMove.TREE);
            }
            if(betterMove > bestMove) {
                n = 1;
                index = i;
                bestMove = betterMove;
            }
            else if(betterMove == bestMove) {
                n++;
                if(1d/n > Math.random())
                    index = i;
            }
        }
        MoveData moveToPlay = tree.get(index);
        tree = moveToPlay.TREE;
        return moveToPlay.MOVE;
    }
    
    private ArrayList<MoveData> getTree(Move move)
    {
        if(move == null)
            return null;
        for(MoveData potentialMove : tree) {
            if(move.equals(potentialMove.MOVE)) {
                if(potentialMove.TREE == null) {
                    System.out.println("CRISIS");
                    return null;
                }
                else {
                    return potentialMove.TREE;
                }
            }
        }
        System.out.println("CRISIS Returns");
        return null;
    }
        
    private double bestMove(ArrayList<MoveData> tree)
    {
        double bestMove = Integer.MIN_VALUE + 1;
        for(MoveData potentialMove : tree) {
            double betterMove;
            if(potentialMove.TREE == null)
                betterMove = potentialMove.EVAL;
            else
                betterMove = -bestMove(potentialMove.TREE);
            if(betterMove > bestMove)
                bestMove = betterMove;
        }
        return bestMove;
    }
        
    private ArrayList<MoveData> growTree(ArrayList<MoveData> tree, Position pos, int layer) throws NullPieceException, NoKingException, Exception
    {
        if(tree == null)
            return plantTree(pos, layer, LAYER - layer + 1, pos.CHECKERS.size(), null);
        if(layer < 1)
            return tree;
        for(MoveData potentialMove : tree)
        {
            Position newPos = pos.move(potentialMove.MOVE);
            if(potentialMove.TREE == null) {
                if(!newPos.stuck()) {
                    potentialMove.setTree(plantTree(newPos, layer - 1, LAYER - layer + 2, potentialMove.CHECKERS, null));               
                }
            }
            else {
                potentialMove.setTree(growTree(potentialMove.TREE, newPos, layer - 1));
            }
        }
        return tree;
    }
    private ArrayList<MoveData> plantTree(Position pos, int layer, int trueLayer, int[] captureLocation) throws Exception
    {
        if(layer < 1)
            return null;
        ArrayList<MoveData> tree = new ArrayList<>();
        for(int i = 0; i < pos.allPieces.size(); i++) {
            Piece piece = pos.allPieces.get(i);
            if(piece.colour != pos.turn) {
                continue;
            }
            ArrayList<int[]> moves = piece.movableTo(pos);
            for(int[] move : moves) {
				
                if(captureLocation != null  &&  (move[0] != captureLocation[0]  ||  move[1] != captureLocation[1]))
                    continue;
                
                boolean capture = false, doubleMove = false;
                ArrayList<Piece> newPieces = (ArrayList<Piece>) pos.allPieces.clone();
                newPieces.remove(i);
                
                //capture...
                for(int j = 0; j < newPieces.size(); j++) {
                    Piece enemyPiece = newPieces.get(j);
                    if(move[0] == enemyPiece.rank  &&  move[1] == enemyPiece.file) {
                        capture = true;
                        newPieces.remove(j);
                        break;
                    }
                }
                //castling...
                if(piece instanceof King  &&  Math.abs(move[1] - piece.file) == 2) {
                    int rookFile = move[1] == 2 ? 0 : 7;
                    for(int j = 0; j < newPieces.size(); j++) {
                        Piece rook = newPieces.get(j);
                        if(rook.rank == piece.rank  &&  rook.file == rookFile) {
                            newPieces.remove(j);
                            newPieces.add(rook.move(piece.rank, rookFile == 0 ? 3 : 5));
                            break;
                        }
                    }
                }
                //en passant and double moving...
                if(piece instanceof Pawn) {
                    if(piece.file != move[1]  &&  pos.board[move[0]][move[1]] == null) {
                        capture = true;
                        Piece p = pos.board[piece.rank][move[1]];
                        newPieces.remove(p);
                    }
                    else if(Math.abs(move[0] - piece.rank) == 2) {
                        doubleMove = true;
                    }
                }
                
                Piece movedPiece = piece.move(move);
                newPieces.add(movedPiece);
                
                Position newPosition = new Position(newPieces, -pos.turn, doubleMove ? (Pawn) movedPiece : null);
                capture = capture  &&  newPosition.underCheck(movedPiece);
                int newLayer = layer > 1 ? layer - 1 : (newPosition.CHECK > 0 ? 2 : (capture ? 1 : 0));
                
                MoveData possibleMove = new MoveData(piece, move, newPosition.eval());
                if(!capture  ||  newPosition.CHECK)
                    move = null;
                //for(int j = 1; j < trueLayer; j++)
                //    System.out.print('\t');
                //System.out.println(trueLayer + possibleMove.toString(pos.board));
                possibleMove.setTree(plantTree(newPosition, /*newLayer*/ layer - 1, trueLayer + 1, move));
                tree.add(possibleMove);
            }
        }
        //System.out.println("--------------LAYER " + trueLayer + "---------------");
        return tree;  
    }
    
    private void printTree(ArrayList<MoveData> tree, int layer)
    {
        if(layer > 1)
            return;
        for(MoveData m : tree)
        {
            for(int i = 0; i < layer; i++)
                System.out.print('\t');
            System.out.printf("%s %.2f\n", m.toString(), m.EVAL);
            if(m.TREE != null)
                printTree(m.TREE, layer + 1);
        }
    }
}
