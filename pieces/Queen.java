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
    
        return  rank == this.rank
            ||  file == this.file
            ||  this.rank - this.file == rank - file
            ||  this.rank + this.file == rank + file;
    }
    @Override
    public boolean isEyeing(Position pos, int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
    
        int rJump = 0, fJump = 0;
        
        if(rank == this.rank)
            fJump = file > this.file ? +1 : -1;
        else if(file == this.file)
            rJump = rank > this.rank ? +1 : -1;
        else if(this.rank - this.file == rank - file)
            rJump = fJump = file > this.file ? +1 : -1;
        else if(this.rank + this.file == rank + file)
            rJump = -(fJump = file > this.file ? +1 : -1);
        else
            return false;
        
        return clear(pos, rank, file, rJump, fJump);
    }

    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return ALL_DIRECTIONS; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }
    
}
