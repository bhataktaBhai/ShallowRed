package pieces;

import chess.Utils;
import chess.Position;
import java.util.ArrayList;

public abstract class Piece
{
    public final char symbol;
    public final int colour;
    public final int rank;
    public final int file;
    public static final int[][] ALL_DIRECTIONS = {{0,+1},{+1,+1},{+1,0},{+1,-1},{0,-1},{-1,-1},{-1,0},{-1,+1}};

    /**
     * The traditional value of each piece in units of pawns.
     * This does not take into account any positional considerations.
     * <p>
     * Queen:   9 pawns <br>
     * Rook:    5 pawns <br>
     * Bishop:  3.2 pawns <br>
     * Knight:  3 pawns <br>
     * Pawn:    1 pawn (obviously) <br>
     * King:    Infinite (<code>Integer.MAX_VALUE</code>) <br>
     * </p>
     * @return  The value of a piece in units of pawns
     */
    public abstract float value();
    
    /**
     * Returns an array of one step a piece can take in any direction.
     * <br>
     * A step is represented by 2 <code>int</code> values in an array, each
     * representing displacement in the y ({@link #rank}) and x ({@link #file})
     * directions respectively.
     * <p>
     * If the piece {@link #isFreelyMoving()}, it can take any number of steps
     * per move in a specific direction (<code>Queen, Rook, Bishop</code>) while
     * obeying the laws of Chess, whereas a <code>Knight, King, Pawn</code> can
     * only take one step in any direction per move.
     * </p>
     * The order of the steps should not matter, but it is in the anticlockwise
     * direction from the positive x-axis.
     * 
     * @return  An int[][] containing ordered pairs representing
     *          displacement in y and x directions for one step by any piece.
     * @see     #rank 
     * @see     #file 
     * @see     #isFreelyMoving() 
     */
    public abstract int[][] steps();
    
    /**
     * Returns whether the <code>Piece</code> can take multiple {@link #steps}
     * in one move or not.
     * <br>
     * <code>Queen, Rook, Bishop</code> have no bounds on movement, whereas the 
     * <code>Knight, King, Pawn</code> can only move one step. Although the
     * <code>Knight</code>'s step is quite long, it counts as one step since he
     * cannot repeat it any number of times in one move.
     * 
     * @return  Whether the piece type can take any number of steps in one move.
     *          true for Queen, Rook, Bishop and false for Knight, King, Pawn
     * @see     #steps() 
     */
    public abstract boolean isFreelyMoving();
    public abstract float developmentPoints
        (Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns, ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces);
        
    /**
     * Returns whether a specified square is in the <code>Piece</code>'s line
     * of sight or not. The <code>rank</code> argument is the ordinate of the
     * square and the <code>file</code> argument is its abscissa.
     * <p>
     * Line of sight here means the squares controlled by a Piece, i.e., where
     * the enemy King cannot tread. 
     * If there is any other <code>Piece</code> between this Piece's location
     * and the target square, this method returns <code>false</code>.
     * <br>
     * However, it returns <code>true</code> even if the Piece is pinned and 
     * cannot move or the target square is occupied by a friendly Piece.
     * </p>
     * Thus, this method is not a substitute for {@link #canMoveTo} but is 
     * faster and can be useful in detecting checks, etc.
     * 
     * @param pos       The position this Piece is in, for detecting obstructions
     * @param rank      The {@link #rank} of the square to be checked
     * @param file      The {@link #file} of the square to be checked
     * 
     * @return          true if the specified square is in the Piece's line of
     *                  sight, false if it is not
     * @see             #canMoveTo(chess.Position, int, int) 
     */
    public abstract boolean isEyeing(Position pos, int rank, int file);
    
    /**
     * Returns whether a specified square would be in the <code>Piece</code>'s 
     * line of sight on an empty chessboard. The <code>rank</code> argument is 
     * the ordinate of the square and the <code>file</code> argument is its
     * abscissa.
     * <p>
     * If it is known that there are no obstructions between this Piece and the
     * target square, this method can be used in place of {@link #isEyeing}. It
     * is much faster since it uses only conditionals and does not have to
     * iterate over the board.
     * Even if it is known that there are no obstruction, this method has the
     * same shortcomings as {@link #isEyeing} and cannot replace
     * {@link #canMoveTo}.
     * </p>
     * For pieces that cannot move freely (<code>Knight, King, Pawn</code>),
     * this method is the same as {@link #isEyeing}.
     * @param rank      The {@link #rank} of the square to be checked
     * @param file      The {@link #file} of the square to be checked
     * 
     * @return          true if the specified square is in the Piece's line of
     *                  sight on an empty chessboard, false if it is not
     * @see             #isEyeing(chess.Position, int, int)
     */
    public abstract boolean mightBeEyeing(int rank, int file);
    //public abstract boolean canMoveInDirection(int rJump, int fJump);
    
    Piece(char symbol, int rank, int file)
    {
        this.symbol = symbol;
        this.colour = symbol < '♚' ? 1 : -1;
        this.rank = rank;
        this.file = file;
    }
    
    public static Piece getPiece(char symbol, int rank, int file, boolean hasMoved)
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
        return null;
    }

    public ArrayList<int[]> eyeing(Position pos)                           //Optimised.
    {
        ArrayList<int[]> eye = new ArrayList<>();
        //Check all directions the piece can move in
        for(int[] step : steps()) {
            int[] move = {rank + step[0], file + step[1]};
            //keep moving while still on board
            //change to if for single-movers
            while(Utils.exists(move[0], move[1])) {
                eye.add(move);
                //exit when encounter a piece, AFTER adding to the list
                if(pos.board[move[0]][move[1]] != null) {
                    break;
                }
                //DO NOT MODIFY THE ALREADY ADDED ARRAY!
                move = new int[]{move[0] + step[0], move[1] + step[1]};
            }
        }
        return eye;
    }
    public ArrayList<Piece> eyeingEnemies(Position pos)                    //Optimised.
    {
        ArrayList<Piece> eye = new ArrayList<>();
        //Check all directions the piece can move in
        for(int[] step : steps()) {
            int[] move = {rank + step[0], file + step[1]};
            //keep moving while still on board
            //change to if for single-movers
            while(Utils.exists(move[0], move[1])) {
                Piece p = pos.board[move[0]][move[1]];
                if(p != null) {
                    if(p.colour != colour)
                        eye.add(p);
                    //exit when encounter a piece, AFTER adding to the list
                    break;
                }
                //can modify the array since it is not to be added to the list
                move[0] += step[0];
                move[1] += step[1];
            }
        }
        return eye;
    }
    
    public ArrayList<int[]> movableTo(Position pos)
    {
        King king = pos.king;
        
        if(pos.CHECKERS.size() > 1) {
            return new ArrayList<>(0);
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
            if(nearestPiece(pos, rJump, fJump) == king) {
                Piece enemy = nearestPiece(pos, -rJump, -fJump);
                pinned = (enemy != null  &&  enemy.colour != colour  &&  enemy.mightBeEyeing(rank, file));
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

    public Piece move(int[] newLocation)
    {
        return move(newLocation[0], newLocation[1]);
    }
    public Piece move(int rank, int file)
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
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Piece)
            return equals((Piece)obj);
        return false;
    }
    public boolean equals(Piece p)
    {
        return (p != null  &&  p.symbol == this.symbol  &&  p.rank == this.rank  &&  p.file == this.file);
    }


    public boolean hasMoved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
