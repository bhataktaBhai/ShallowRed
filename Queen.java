package pieces;

import chess.Position;
import java.util.ArrayList;

public class Queen extends Piece
{
    private static final int VAL = 9;
    private static final boolean FREELY_MOVING = true;
    
    public Queen(char symbol, int rank, int file)
    {
        super(symbol, rank, file);
    }
    
    @Override
    public float developmentPoints
        (Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns,
         ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces)
    {
        return 0;
    }
    
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
    
        return (rank == this.rank) || (file == this.file) ||
            (this.rank - this.file == rank - file) || (this.rank + this.file == rank + file);
    }
    @Override
    public boolean isEyeing(Position pos, int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
    
        if(rank == this.rank) {
            int jump = file > this.file ? +1 : -1;
            int f = this.file + jump;
            while(f != file) {
                if(pos.board[rank][f] != null)
                    return false;
                f += jump;
            }
            return true;
        }
        else if(file == this.file) {
            int jump = rank > this.rank ? +1 : -1;
            int r = this.rank + jump;
            while(r != rank) {
                if(pos.board[r][file] != null)
                    return false;
                r += jump;
            }
            return true;
        }
        else if(this.rank - this.file == rank - file) {
            int rJump, fJump, r, f;
            if(this.rank < rank)
                rJump = fJump = +1;
            else
                rJump = fJump = -1;
            r = this.rank + rJump;  f = this.file + fJump;
            while(r != rank) {
                if(pos.board[r][f] != null) 
                    return false;
                r += rJump;     f += fJump;
            }
            return true;
        }
        else if(this.rank + this.file == rank + file) {
            int rJump, fJump, r, f;
            if(this.rank < rank) {
                rJump = +1;     fJump = -1;
            }
            else {
                rJump = -1;     fJump = +1;
            }
            r = this.rank + rJump;  f = this.file + fJump;
            while(r != rank) {
                if(pos.board[r][f] != null) 
                    return false;
                r += rJump;     f += fJump;
            }
            return true;
        }
        return false;
    }

    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return ALL_DIRECTIONS; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }
    
}