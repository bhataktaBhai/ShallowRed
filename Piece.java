package pieces;

import chess.Utils;
import chess.Position;
import except.NullPieceException;
import java.util.ArrayList;

public abstract class Piece
{
    public char symbol;
    public final int colour;
    public final int rank;
    public final int file;
    public static final int[][] ALL_DIRECTIONS = {{0,+1},{+1,+1},{+1,0},{+1,-1},{0,-1},{-1,-1},{-1,0},{-1,+1}};

    public abstract float value();
    public abstract int[][] steps();    //anticlockwise from +ve x-axis.
    public abstract boolean isFreelyMoving();
    public abstract float developmentPoints
        (Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns, ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces);
    public abstract boolean isEyeing(Position pos, int rank, int file);
    public abstract boolean mightBeEyeing(int rank, int file);
    //public abstract boolean canMoveInDirection(int rJump, int fJump);
    
    Piece(char symbol, int rank, int file)
    {
        this.symbol = symbol;
        this.colour = symbol < '♚' ? 1 : -1;
        this.rank = rank;
        this.file = file;
    }
    
    public static Piece getPiece(char symbol, int rank, int file, boolean hasMoved) throws NullPieceException
    {
        int typeOfPiece = (symbol - '♔') % 6;
        switch(typeOfPiece) {
            case 0:
                return new King(symbol, rank, file, hasMoved);
            case 1:
                return new Queen(symbol, rank, file);
            case 2:
                return new Rook(symbol, rank, file, hasMoved);
            case 3:
                return new Bishop(symbol, rank, file);
            case 4:
                return new Knight(symbol, rank, file);
            case 5:
                return new Pawn(symbol, rank, file);
        }
        throw new except.NullPieceException();
    }

    public ArrayList<int[]> eyeing(Position pos)                           //Optimised.
    {
        ArrayList<int[]> eye = new ArrayList<>();
        for(int[] step : steps()) {
            int[] move = {rank + step[0], file + step[1]};
            while(Utils.exists(move[0], move[1])) {
                eye.add(move);
                if(pos.board[move[0]][move[1]] != null) {
                    break;
                }
                move = new int[]{move[0] + step[0], move[1] + step[1]};
            }
        }
        return eye;
    }
    public ArrayList<Piece> eyeingEnemies(Position pos)                    //Optimised.
    {
        ArrayList<Piece> eye = new ArrayList<>();
        for(int[] step : steps()) {
            int[] move = {rank + step[0], file + step[1]};
            while(Utils.exists(move[0], move[1])) {
                Piece p = pos.board[move[0]][move[1]];
                if(p != null) {
                    if(p.colour != colour)
                        eye.add(p);
                    break;
                }
            }
        }
        return eye;
    }
    
    public ArrayList<int[]> movableTo(Position pos)
    {
        King king = pos.king;
        
        if(pos.CHECKERS.size() > 1) {
            return new ArrayList<>();
        }
        
        //finds direction of king to piece
        //error-free (no methods called)
        boolean pinned = false;
        int rJump = 0, fJump = 0;
        if(king.rank == this.rank) {
            fJump = king.file > this.file ? +1 : -1;
        }
        else if(king.file == this.file) {
            rJump = king.rank > this.rank ? +1 : -1;
        }
        //y - x = c  =>  slope = 1
        else if(king.rank - king.file == this.rank - this.file) {
            rJump = fJump = king.rank > this.rank ? +1 : -1;
        }
        //y + x = c  =>  slope = -1
        else if(king.rank + king.file == this.rank + this.file) {
            rJump = king.rank > this.rank ? +1 : -1;
            fJump = -rJump;
        }
        
        //determines if piece is pinned to king
        //error-free (excl. methods called)
        if(rJump != 0  ||  fJump != 0) {
            int r = this.rank + rJump;
            int f = this.file + fJump;
            while(r != king.rank  ||  f != king.file) {
                if(pos.board[r][f] != null)
                    break;  //effectively breaks out of if()
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
        
        //handles all checking cases, makes sense
        //error-free (excl. methods called)
        if(pos.CHECK) {
            
            if(pinned) {
                return legalMoves;
            }
            
            Piece checker = pos.CHECKERS.get(0);
            if(checker.isFreelyMoving()) {
                ArrayList<int[]> blocks = Utils.squaresBetween(king.rank, king.file, checker.rank, checker.file);
                for(int[] square : blocks) {
                    if(this.isEyeing(pos, square[0], square[1]))
                        legalMoves.add(square);
                }
            }
            
            if(this.isEyeing(pos, checker.rank, checker.file)){
                legalMoves.add(new int[]{checker.rank, checker.file});
            }
            
            return legalMoves;
        }
        
        //error-free (incl. methods called)
        for(int[] step : steps()) {
            if(pinned) {
                if(!(step[0] ==  rJump  &&  step[1] ==  fJump  ||  step[0] == -rJump  &&  step[1] == -fJump))
                    continue;
            }
            int[] move = {rank + step[0], file + step[1]};
            while(Utils.exists(move[0], move[1])) {
                if(pos.board[move[0]][move[1]] == null) {
                    legalMoves.add(move);
                }
                else {
                    if(pos.board[move[0]][move[1]].colour != this.colour) {
                        legalMoves.add(move);
                    }
                    break;
                }
                move = new int[]{move[0] + step[0], move[1] + step[1]};
            }
        }
        
        return legalMoves;
    }
    
    public boolean pinned(Position pos)
    {
        King king = pos.king;
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
        else {
            return false;
        }
        
        if(nearestPiece(pos, rJump, fJump) != king)
            return false;
        
        Piece enemy = nearestPiece(pos, -rJump, -fJump);
        return (enemy != null  &&  enemy.colour != colour  &&  enemy.mightBeEyeing(rank, file));
    }
    
    public boolean obstruction(Position pos, int rank, int file, int rJump, int fJump)
    {
        int r = this.rank + rJump;
        int f = this.file + fJump;
        while(r != rank  ||  f != file) {
            if(pos.board[r][f] != null)
                return true;
            r += rJump;
            f += fJump;
        }
        return false;
    }
    public Piece nearestPiece(Position pos, int rJump, int fJump)
    {
        int r = this.rank + rJump;
        int f = this.file + fJump;
        while(Utils.exists(r, f)) {
            if(pos.board[r][f] != null)
                return pos.board[r][f];
            r += rJump;
            f += fJump;
        }
        return null;
    }
    
    public boolean canMoveTo(Position pos, int rank, int file)
    {
        for(int[] movableSquare : movableTo(pos)) {
            if(rank == movableSquare[0]  &&  file == movableSquare[1]) {
                return true;
            }
        }
        return false;
    }

    public Piece move(int[] newLocation) throws NullPieceException
    {
        return move(newLocation[0], newLocation[1]);
    }
    public Piece move(int rank, int file) throws NullPieceException
    {
        Piece p = getPiece(symbol, rank, file, true);
        return p;
    }
    
    public int relativeRank() { return colour == 1 ? rank : 7 - rank; }
    
    public boolean instanceOf(char c)
    {
        switch(c) {
            case 'B':
                return (this instanceof Bishop);
            case 'K':
                return (this instanceof King);
            case 'N':
                return (this instanceof Knight);
            case 'P':
                return (this instanceof Pawn);
            case 'Q':
                return (this instanceof Queen);
            case 'R':
                return (this instanceof Rook);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return Character.toString(symbol);
    }
    
    public boolean equals(Piece p)
    {
        return (p == null ? false : (p.symbol == this.symbol  &&  p.rank == this.rank  &&  p.file == this.file));
    }

    public boolean hasMoved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}