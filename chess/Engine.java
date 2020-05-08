package chess;

import pieces.King;
import pieces.Pawn;
import pieces.Piece;
import java.util.ArrayList;

public class Engine
{
    private ArrayList<MoveData> tree;
    private final int LAYER = 3;
    
        class MoveData
        {
            private final Position pos;
            private ArrayList<MoveData> moreMoves;
            private String move;

            MoveData(Position pos, Piece piece, int[] move)
            {
                this.pos = pos;
                this.move = piece.toString() + (char)(move[1] + 'a') + (move[0] + 1);
                if(move.length > 2)
                    this.move += (char) move[2];
            }

            void setTree(ArrayList<MoveData> moreMoves) {
                this.moreMoves = moreMoves;
            }

            Position pos() {
                return pos;
            }
            ArrayList<MoveData> tree() {
                return moreMoves;
            }
            @Override
            public String toString() {
                return move;
            }
        }

    public Position play(Position pos)
    {
        tree = getTree(pos);
        tree = growTree(tree, LAYER);
        //printTree(tree, 0);
        double bestMove = Integer.MIN_VALUE + 1;
        int index = 0;
        for(int i = 1; i < tree.size(); i++) {
            MoveData possibleMove = tree.get(i);
            double betterMove;
            if(possibleMove.tree() == null) {
                System.out.println("Excuse me?");
                betterMove = possibleMove.pos().eval();
            }
            else {
                betterMove = - bestMove(possibleMove.tree());
            }
            if(betterMove > bestMove) {
                index = i;
                bestMove = betterMove;
            }
        }
        MoveData newPosition = tree.get(index);
        tree = newPosition.tree();
        return newPosition.pos();
    }
    
    
    private ArrayList<MoveData> getTree(Position pos)
    {
        if(tree == null) {
            return plantTree(pos, LAYER);
        }
        for(int i = 0; i < tree.size(); i++) {
            MoveData position = tree.get(i);
            if(position.pos().equals(pos)) {
                if(position.tree() != null) {
                    return position.tree();
                }
                else {
                    System.out.println("SCARY!");
                    return plantTree(pos, LAYER);
                }
            }
        }
        System.out.println("CRISIS");
        return null;
    }
    
    private ArrayList<MoveData> growTree(ArrayList<MoveData> tree, int layer)
    {
        for(MoveData possibleMove : tree) {
            if(possibleMove.tree() == null) {
                if( possibleMove.pos().stuck() ) {
                    continue;
                }
                ArrayList<MoveData> nextPossibleMoves = plantTree(possibleMove.pos(), layer - 1);
                possibleMove.setTree(nextPossibleMoves);
            }
            else {
                ArrayList<MoveData> nextPossibleMoves = growTree(possibleMove.tree(), layer - 1);
                possibleMove.setTree(nextPossibleMoves);
            }
        }
        return tree;
    }
    private ArrayList<MoveData> plantTree(Position pos, int layer)
    {
        if(layer < 1)
            return null;
        //System.out.println("Initialising tree...");
        ArrayList<MoveData> tree = new ArrayList<>();
        //Chess.print(pos.board);
        for(int i = 0; i < pos.allPieces.size(); i++) {
            Piece piece = pos.allPieces.get(i);
            if(piece.colour != pos.turn) {
                continue;
            }
            ArrayList<int[]> moves = piece.movableTo(pos);
            for(int[] move : moves) {
                
                boolean capture = false, forcing = false, doubleMove = false;
                ArrayList<Piece> newPieces = (ArrayList<Piece>) pos.allPieces.clone();
                Piece newPiece = newPieces.get(i);
                
                newPieces.remove(i);
                
                //capture...
                for(Piece piece2 : newPieces) {
                    if(move[0] == piece2.rank  &&  move[1] == piece2.file) {
                        capture = true;
                        newPieces.remove(piece2);
                        break;
                    }
                }
                //castling...
                if(newPiece instanceof King  &&  Math.abs(move[1] - newPiece.file) == 2) {
                    int rookFile = move[1] == 2 ? 0 : 7;
                    for(int j = 0; j < newPieces.size(); j++) {
                        Piece rook = newPieces.get(j);
                        if(rook.rank == newPiece.rank  &&  rook.file == rookFile) {
                            newPieces.remove(j);
                            newPieces.add(rook.move(newPiece.rank, rookFile == 0 ? 3 : 5));
                            break;
                        }
                    }
                }
                //en passant...
                if(newPiece instanceof Pawn)
                {
                    if(newPiece.file != move[1]  &&  pos.board[move[0]][move[1]] == null) {
                        capture = true;
                        Piece p = pos.board[newPiece.rank][move[1]];
                        newPieces.remove(p);
                    }
                    else if(Math.abs(move[0] - newPiece.rank) == 2) {
                        doubleMove = true;
                    }
                }
                
                Piece movedPiece = newPiece.move(move);
                newPieces.add(movedPiece);
                
                Position newPosition = new Position (newPieces, -pos.turn, doubleMove ? (Pawn) movedPiece : null);
                if(newPosition.CHECK)
                    forcing = true;
                else if(capture) {
                    forcing = newPosition.underCheck(movedPiece);
                }
                MoveData possibleMove = new MoveData(newPosition, newPiece, move);
                possibleMove.setTree(plantTree(newPosition, forcing  &&  layer < 2 ? layer : layer - 1));
                tree.add(possibleMove);
            }
        }
        return tree;    
    }
    
    private double bestMove(ArrayList<MoveData> tree)
    {
        double bestMove = Integer.MIN_VALUE + 1;
        for(MoveData possibleMove : tree) {
            double betterMove;
            if(possibleMove.tree() == null) {
                //System.out.println(tree.getClass());
                //System.out.println(position.getClass());
                betterMove =  possibleMove.pos().eval();
            }
            else {
                betterMove = - bestMove(possibleMove.tree());
            }
            if(betterMove > bestMove) {
                bestMove = betterMove;
            }
        }
        return bestMove;
    }
    
    private void printTree(ArrayList<MoveData> tree, int layer)
    {
        for(MoveData move : tree)
        {
            for(int i = 0; i < layer; i++)
                System.out.print("\t");
            System.out.printf("%s: %.1f\n", move, move.pos().eval());
            if(move.tree() != null)
                printTree(move.tree(), layer + 1);
        }
    }
    
    /*Position move0(int numOfMoves)
    {
        if(numOfMoves == 0  ||  pos.stuck()) {
            return null;
        }
        //ArrayList<Piece> movers = new ArrayList<>();
        ArrayList<int[]> moves = new ArrayList<>();
        for(int i = 0; i < allPieces.size(); i++) {
            Piece piece = allPieces.get(i);
            if(piece.colour != colour) {
                continue;
            }
            for(int[] move : piece.movableTo(pos)) {
                ArrayList<Piece> newPieces = (ArrayList<Piece>) allPieces.clone();
                Piece newPiece = newPieces.get(i);
                for(Piece capture : newPieces) {
                    if(move[0] == capture.rank  &&  move[1] == capture.file) {
                        newPieces.remove(capture);
                        break;
                    }
                }
                newPieces.remove(i);
                newPieces.add(newPiece.move(move));
                Position position = new Engine(new Position(newPieces, -colour)).move0(numOfMoves - 1);
                int evaluation = position == null ? -1048576 : (int)position.evaluation();
                moves.add(new int[]{piece.rank, piece.file, move[0], move[1], move.length > 2 ? move[2] : 0, evaluation});
            }
        }
        int maxEval = moves.get(0)[4];
        int max = 0;
        for(int i = 1; i < moves.size(); i++) {
            if(moves.get(i)[5] > maxEval) {
                maxEval = moves.get(i)[5];
                max = i;
            }
        }
        int[] bestMove = moves.get(max);
        int[] origin = new int[]{bestMove[0], bestMove[1]};
        int[] destin = new int[]{bestMove[2], bestMove[3], bestMove[4]};
        //System.out.println(Arrays.toString(origin) + "\n" + Arrays.toString(destin));
        ArrayList<Piece> newPieces = (ArrayList<Piece>) allPieces.clone();
        int removal = -1;
        for(int i = 0; i < newPieces.size(); i++) {
            Piece piece = newPieces.get(i);
            if(piece.rank == destin[0]  &&  piece.file == destin[1]) {
                removal = i;
            }
            if(piece.rank == origin[0]  &&  piece.file == origin[1]) {
                //System.out.println(piece);
                newPieces.remove(i);
                newPieces.add(i, piece.move(destin));
            }
        }
        if(removal != -1) {
            newPieces.remove((int)removal);
        }
        return new Position(newPieces, -colour);
    }*/
    
    /*growTree ka purana version:
        if(tree.size() == 1) {
            if(layer < 1) {
                return tree;
            }
            MoveData aaaaaaaa = tree.get(0);
            Position pos =  aaaaaaaa.pos();
            //Chess.print(position.board);
            ArrayList<MoveData> subTree = new ArrayList<>();
            aaaaaaaa.add(subTree);
            for(int i = 0; i < pos.allPieces.size(); i++) {
                Piece piece = pos.allPieces.get(i);
                if(piece.colour != pos.turn) {
                    continue;
                }
                ArrayList<int[]> moves = piece.movableTo(pos);
                for(int[] move : moves) {
                    ArrayList<Piece> newPieces = (ArrayList<Piece>) pos.allPieces.clone();
                    Piece newPiece = newPieces.get(i);
                    newPieces.remove(i);
                    for(Piece capture : newPieces) {
                        if(move[0] == capture.rank  &&  move[1] == capture.file) {
                            newPieces.remove(capture);
                            break;
                        }
                    }
                    for(Piece p : newPieces) {
                        p.hasJustMovedDouble = false;
                    }
                    newPieces.add(newPiece.move(move));
                    if(newPiece instanceof King  &&  Math.abs(move[1] - newPiece.file) == 2) {
                        int rookFile = move[1] == 2 ? 0 : 7;
                        for(int j = 0; j < newPieces.size(); j++) {
                            Piece rook = newPieces.get(j);
                            if(rook.rank == newPiece.rank  &&  rook.file == rookFile) {
                                newPieces.remove(j);
                                newPieces.add(rook.move(move));
                                break;
                            }
                        }
                    }
                    
                    Position newPosition = new Position (newPieces, -pos.turn);
                    MoveData newMove = new MoveData(newPosition);
                    if( !newPosition.stuck()  &&  layer > 1 ) {
                        newMove.addTree(ERRROOOORRRR);//setTree(newMove.toList(), layer - 1));
                    }
                    
                    subTree.add(newMove);
                }
            }
            return subTree;
        }*/
}
