package pieces;

import chess.Position;
import java.util.ArrayList;

public class Bishop extends Piece
{
    private static final float VAL = 3.2f;
    private static final boolean FREELY_MOVING = true;
    private static final int[][] STEPS = {{+1,+1},{-1,+1},{-1,-1},{+1,-1}};
    
    public Bishop(char symbol, int rank, int file)
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
    
    //@Override
    public boolean canMoveInDirection(int rJump, int fJump)
    {
        return rJump != 0  &&  (rJump == fJump  ||  rJump == -fJump);
    }
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        if(rank == this.rank)
            return false;
    
        //      y - x = c  =>  slope = 1                  y + x = c  =>  slope = -1
        return (this.rank - this.file == rank - file) || (this.rank + this.file == rank + file);
    }
    @Override
    public boolean isEyeing(Position pos, int rank, int file)
    {
        if(rank == this.rank  ||  file == this.file)
            return false;
        
        if(this.rank - this.file == rank - file) {
            int rJump, fJump;
            if(this.rank < rank)
                rJump = fJump = +1;
            else
                rJump = fJump = -1;
            return !obstruction(pos, rank, file, rJump, fJump);
        }
        else if(this.rank + this.file == rank + file) {
            int rJump, fJump;
            if(this.rank < rank)
                fJump = -(rJump = 1);
            else
                rJump = -(fJump = 1);
            return !obstruction(pos, rank, file, rJump, fJump);
        }
        return false;
    }
    
    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return STEPS; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }
    
}
