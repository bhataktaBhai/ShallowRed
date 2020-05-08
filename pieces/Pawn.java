package pieces;

import chess.Utils;
import chess.Position;
import except.NullPieceException;
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
    public ArrayList<int[]> eyeing(Position pos)                           //Optimised.
    {
        ArrayList<int[]> eye = new ArrayList<>();
        if(file > 0) {
            eye.add(new int[]{rank + colour, file - 1});
        }
        if(file < 7) {
            eye.add(new int[]{rank + colour, file + 1});
        }
        return eye;
    }
    
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
    
        return (this.rank + colour == rank)  &&  Math.abs(this.file - file) == 1;
    }
    @Override
    public boolean isEyeing(Position notRequired, int rank, int file)       //Optimised.
    {
        return mightBeEyeing(rank, file);
    }
    @Override
    public ArrayList<Piece> eyeingEnemies(Position pos)                    //Optimised.
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
    
    @Override
    public ArrayList<int[]> movableTo(Position pos)
    {
        King king = pos.king;
        
        if(pos.CHECKERS.size() > 1) {
            return new ArrayList<>();
        }
        
        boolean pinned = false;
        int rJump = 0, fJump = 0;
        if(king.rank == this.rank) {
            fJump = king.file > this.file ? +1 : -1;
        }
        else if(king.file == this.file) {
            rJump = king.rank > this.rank ? +1 : -1;
        }
        else if(king.rank - king.file == this.rank - this.file) {
            rJump = fJump = king.rank > this.rank ? +1 : -1;
        }
        else if(king.rank + king.file == this.rank + this.file) {
            rJump = king.rank > this.rank ? +1 : -1;
            fJump = -rJump;
        }
        if(rJump != 0  ||  fJump != 0) {
            int r = this.rank + rJump;
            int f = this.file + fJump;
            while(r != king.rank  ||  f != king.file) {
                if(pos.board[r][f] != null)
                    break;
                r += rJump;
                f += fJump;
            }
            if(r == king.rank  &&  f == king.file) {
                r = this.rank - rJump;
                f = this.file - fJump;
                while(Utils.exists(r, f)) {
                    Piece enemySuspect = pos.board[r][f];
                    if(enemySuspect != null) {
                        if(enemySuspect.colour != colour  &&  enemySuspect.isFreelyMoving()) {
                            if(enemySuspect.mightBeEyeing(rank, file)) {
                                pinned = true;
                            }
                        }
                        break;
                    }
                    r -= rJump;
                    f -= fJump;
                }
            }
        }
        
        ArrayList<int[]> legalMoves = new ArrayList<>();
        int highRank = rank + colour;
        int rightFile = file + colour;
        int leftFile = file - colour;
        
        if(pos.CHECK) {
            if(pinned)
                return legalMoves;
            
            Piece checker = pos.CHECKERS.get(0);
            if(checker.isFreelyMoving()) {
                ArrayList<int[]> blocks = Utils.squaresBetween(king.rank, king.file, checker.rank, checker.file);
                for(int square[] : blocks) {
                    if(file == square[1]) {
                        if(highRank == square[0])
                            legalMoves.add(square);
                        else if(highRank + colour == square[0]  &&  pos.board[highRank][file] == null)
                            legalMoves.add(square);
                        break;
                    }
                }
            }
            
            if(this.isEyeing(pos, checker.rank, checker.file)) {
                legalMoves.add(new int[]{checker.rank, checker.file});
            }
            
            else if(checker == pos.doubleMover  &&  checker.rank == rank) {
                if(checker.file == rightFile  ||  checker.file == leftFile)
                    legalMoves.add(new int[]{highRank, checker.file});
            }
            
            return legalMoves;
        }
        
        
        //moving forward.
        if(!pinned  ||  fJump == 0) {
            if(pos.board[rank + colour][file] == null) {
                if(relativeRank() == 6) {
                    legalMoves.add(new int[]{highRank, file, 1});
                    legalMoves.add(new int[]{highRank, file, 2});
                    legalMoves.add(new int[]{highRank, file, 3});
                    legalMoves.add(new int[]{highRank, file, 4});
                }
                else {
                    legalMoves.add(new int[]{highRank, file});
                    if(relativeRank() == 1  &&  pos.board[highRank + colour][file] == null) {
                        legalMoves.add(new int[]{highRank + colour, file});
                    }
                }
            }
        }
        
        //enPassant
        if(pos.doubleMover != null  &&  pos.doubleMover.rank == this.rank) {
            if(pos.doubleMover.file == leftFile) {
                if(!pinned  ||  rJump == -fJump) {
                    legalMoves.add(new int[]{highRank, leftFile});
                }
            }
            if(pos.doubleMover.file == rightFile) {
                if(!pinned  ||  rJump == fJump) {
                    legalMoves.add(new int[]{highRank, rightFile});
                }
            }
        }
        
        //capturing forward left (from player's perspective).
        if(!pinned  ||  rJump == -fJump) {
            Piece enemySuspect = Utils.get(pos.board, highRank, leftFile);
            if(enemySuspect != null  &&  enemySuspect.colour != colour) {
                if(relativeRank() == 6) {
                    legalMoves.add(new int[]{highRank, leftFile, 1});
                    legalMoves.add(new int[]{highRank, leftFile, 2});
                    legalMoves.add(new int[]{highRank, leftFile, 3});
                    legalMoves.add(new int[]{highRank, leftFile, 4});
                }
                else {
                    legalMoves.add(new int[]{highRank, leftFile});
                }
            }
        }
        //capturing forward right (from player's perspective).
        if(!pinned  ||  rJump == fJump) {
            Piece enemySuspect = Utils.get(pos.board, highRank, rightFile);
            if(enemySuspect != null  &&  enemySuspect.colour != colour) {
                if(relativeRank() == 6) {
                    legalMoves.add(new int[]{highRank, rightFile, 1});
                    legalMoves.add(new int[]{highRank, rightFile, 2});
                    legalMoves.add(new int[]{highRank, rightFile, 3});
                    legalMoves.add(new int[]{highRank, rightFile, 4});
                }
                else {
                    legalMoves.add(new int[]{highRank, rightFile});
                }
            }
        }
        
        return legalMoves;
    }
    
    @Override
    public Piece move(int[] newLocation) throws NullPieceException
    {
        if(newLocation.length > 2  &&  newLocation[2] != 0) {
            return promote(newLocation[2], newLocation[0], newLocation[1]);
        }
        
        Pawn p = new Pawn(symbol, newLocation[0], newLocation[1]);
        return p;
    }

    Piece promote(int piece, int rank, int file) throws NullPieceException
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
        if(colour == -1) {
            newSymbol += 6;
        }
        return getPiece(newSymbol, rank, file, true);
    }

    public boolean isDoubled(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns) {
            if(pawn.file == file  &&  pawn != this) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isIsolated(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns) {
            if(pawn.file == file + 1  ||  pawn.file == file - 1) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isBackward(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns) {
            if(pawn.file == file + 1  ||  pawn.file == file - 1) {
                if(pawn.relativeRank() <= relativeRank()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public float developmentPoints()
    {
        switch(relativeRank()) {
            case 1:
                return 0;
            case 2:
                switch(file) {
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
                switch(file) {
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
                switch(file) {
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
                switch(file) {
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
                switch(file) {
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
