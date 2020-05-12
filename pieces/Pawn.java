package pieces;

import chess.Move;
import chess.Utils;
import chess.Position;
import java.util.ArrayList;

public class Pawn extends Piece
{
    private static final int VAL = 1;
    private static final boolean FREELY_MOVING = false;
    
    public Pawn(char symbol, int rank, int file)
    {
        super(symbol, rank, file);
    }

    @Override
    public ArrayList<int[]> eyeing(Position pos)
    {
        ArrayList<int[]> eye = new ArrayList<>();
        if(file > 0)
            eye.add(new int[]{rank + colour, file - 1});
        if(file < 7)
            eye.add(new int[]{rank + colour, file + 1});
        return eye;
    }
    
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        return isEyeing(null, rank, file);
    }
    @Override
    public boolean isEyeing(Position notRequired, int rank, int file)
    {
        return (this.rank + colour == rank)
                &&  Math.abs(this.file - file) == 1;
    }
    @Override
    public ArrayList<Piece> eyeingEnemies(Position pos)
    {
        ArrayList<Piece> eye = new ArrayList<>();
        if(file > 0) {
            Piece p = pos.board[rank + colour][file - 1];
            if(p != null  &&  p.colour != colour)
                eye.add(p);
        }
        if(file < 7) {
            Piece p = pos.board[rank + colour][file + 1];
            if(p != null  &&  p.colour != colour)
                eye.add(p);
        }
        return eye;
    }
    
    private static void addMoves(ArrayList<int[]> moves, int rank, int file)
    {
        if(rank == 0  ||  rank == 7)
        {
            moves.add(new int[]{rank, file, 1});
            moves.add(new int[]{rank, file, 2});
            moves.add(new int[]{rank, file, 3});
            moves.add(new int[]{rank, file, 4});
        }
        else
            moves.add(new int[]{rank, file});
    }
    @Override
    public ArrayList<int[]> movableTo(Position pos)
    {
        King king = pos.king;
        
        if(pos.CHECKERS.size() > 1)
            return new ArrayList<>();
        
        int[] pinned = pinned(pos);
        
        ArrayList<int[]> legalMoves = new ArrayList<>();
        final int HIGH_RANK = rank + colour;
        final int RIGHT_FILE = file + colour;
        final int LEFT_FILE = file - colour;
        
        if(pos.CHECK) {
            if(pinned != null)
                return legalMoves;
            
            Piece checker = pos.CHECKERS.get(0);
            if(checker.isFreelyMoving())
            {
                ArrayList<int[]> blocks = Utils.squaresBetween(king.rank, king.file, checker.rank, checker.file);
                for(int square[] : blocks)
                {
                    if(file == square[1])
                    {
                        if(HIGH_RANK == square[0])
                            legalMoves.add(square);
                        else if(relativeRank() == 1
                                &&  HIGH_RANK + colour == square[0]
                                &&  pos.board[HIGH_RANK][file] == null)
                            legalMoves.add(square);
                        break;
                    }
                }
            }
            
            if(this.isEyeing(pos, checker.rank, checker.file))
                legalMoves.add(new int[]{checker.rank, checker.file});
            
            else if(checker == pos.doubleMover  &&  checker.rank == rank)
                if(checker.file == RIGHT_FILE  ||  checker.file == LEFT_FILE)
                    legalMoves.add(new int[]{HIGH_RANK, checker.file});
            
            return legalMoves;
        }
        
        
        //moving forward.
        if(pinned == null  ||  pinned[1] == 0)
        {
            if(pos.board[HIGH_RANK][file] == null)
            {
                addMoves(legalMoves, HIGH_RANK, file);
                if(relativeRank() == 1
                        &&  pos.board[HIGH_RANK + colour][file] == null)
                    addMoves(legalMoves, HIGH_RANK + colour, file);
            }
        }
        
        //enPassant
        if(pos.doubleMover != null  &&  pos.doubleMover.rank == this.rank)
        {
            if(pos.doubleMover.file == LEFT_FILE)
                if(pinned == null  ||  pinned[0] + pinned[1] == 0)
                    legalMoves.add(new int[]{HIGH_RANK, LEFT_FILE});
            if(pos.doubleMover.file == RIGHT_FILE)
                if(pinned == null  ||  pinned[0] - pinned[1] == 0);
                    legalMoves.add(new int[]{HIGH_RANK, RIGHT_FILE});
        }
        
        //capturing forward left (from player's perspective)
        if(pinned == null  ||  pinned[0] + pinned[1] == 0)
        {
            Piece enemySuspect = Utils.get(pos.board, HIGH_RANK, LEFT_FILE);
            if(enemySuspect != null  &&  enemySuspect.colour != colour)
                addMoves(legalMoves, HIGH_RANK, LEFT_FILE);
        }
        
        //capturing forward right (from player's perspective)
        if(pinned == null  ||  pinned[0] - pinned[1] == 0)
        {
            Piece enemySuspect = Utils.get(pos.board, HIGH_RANK, RIGHT_FILE);
            if(enemySuspect != null  &&  enemySuspect.colour != colour)
                addMoves(legalMoves, HIGH_RANK, RIGHT_FILE);
        }
        
        return legalMoves;
    }
    
    @Override
    public Piece move(Move move)
    {
        if(move.promotion() != 0)
            return promote(move.promotion(), move.rank(), move.file());
        
        return move(move.rank(), move.file());
    }

    private Piece promote(int piece, int rank, int file)
    {
        char newSymbol = '\u0000';
        switch(piece) {
            case 1:
                newSymbol = '♕';
                break;
            case 2:
                newSymbol = '♖';
                break;
            case 3:
                newSymbol = '♗';
                break;
            case 4:
                newSymbol = '♘';
                break;
        }
        
        if(colour == -1)
            newSymbol += 6;
        
        return getPiece(newSymbol, rank, file, true);
    }

    public boolean isDoubled(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns)
            if(pawn.file == file  &&  pawn != this)
                return true;
        return false;
    }
    
    public boolean isIsolated(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns)
            if(Math.abs(pawn.file - file) == 1)
                return false;
        return true;
    }
    
    public boolean isBackward(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns)
            if(Math.abs(pawn.file - file) == 1)
                if(pawn.relativeRank() <= this.relativeRank())
                    return false;
        return true;
    }
    
    public float developmentPoints()
    {
        switch(relativeRank())
        {
            case 1:
                return 0;
            case 2:
                switch(file)
                {
                    case 0:
                    case 7:
                        return 0.07f;
                    case 3:
                    case 4:
                        return 0.14f;
                    default:
                        return 0.1f;
                }
            case 3:
                switch(file)
                {
                    case 0:
                    case 7:
                        return 0.07f;
                    case 3:
                    case 4:
                        return 0.2f;
                    case 2:
                    case 5:
                        return 0.15f;
                    default:
                        return 0.1f;
                }
            case 4:
                switch(file)
                {
                    case 0:
                    case 7:
                        return 0.12f;
                    case 1:
                    case 6:
                        return 0.15f;
                    case 2:
                    case 5:
                        return 0.18f;
                    default:
                        return 0.24f;
                }
            case 5:
                switch(file)
                {
                    case 0:
                    case 7:
                        return 0.18f;
                    case 1:
                    case 6:
                        return 0.18f;
                    default:
                        return 0.21f;
                }
            case 6:
                switch(file)
                {
                    case 0:
                    case 7:
                        return 0.6f;
                    case 1:
                    case 6:
                        return 0.45f;
                    default:
                        return 0.3f;
                }
        }
        System.out.println("PAWN ON EDGE RANK!");
        return 9;
    }
    
    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return null; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }

    @Override
    public float developmentPoints
        (Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns,
         ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}