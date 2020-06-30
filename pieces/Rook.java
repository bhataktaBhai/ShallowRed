package pieces;

import chess.Position;
import java.util.ArrayList;

public class Rook extends Piece
{
    private static final int VAL = 5;
    private static final boolean FREELY_MOVING = true;
    private static final int[][] STEPS = {{0,+1},{+1,0},{0,-1},{-1,0}};
    private final boolean HAS_MOVED; 
    
    public Rook(char symbol, int rank, int file, boolean hasMoved)
    {
        super(symbol, rank, file);
        this.HAS_MOVED = hasMoved;
    }
    
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
    
        return rank == this.rank  ||  file == this.file;
    }
    @Override
    public boolean isEyeing(Position pos, int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
        
        if(rank == this.rank)
        {
            int fJump = file > this.file ? +1 : -1;
            return clear(pos, rank, file, 0, fJump);
        }
        else if(file == this.file)
        {
            int rJump = rank > this.rank ? +1 : -1;
            return clear(pos, rank, file, rJump, 0);
        }
        
        return false;
    }
    
    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return STEPS; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }
    @Override
    public boolean hasMoved() { return HAS_MOVED; }
    
    @Override
    public float developmentPoints
        (Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns,
         ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces)
    {
        float rookBonus = 0;
        if(relativeRank() == 6  ||  relativeRank() == 5) {
            for(Piece piece : friendlyPieces) {
                if(piece instanceof Rook  ||  piece instanceof Queen) {
                    if(piece.rank == rank)
                        rookBonus += 0.6;
                    else if(piece.file == file  &&  piece.isEyeing(pos, rank, file))
                        rookBonus += 0.4;
                }
            }
            int c = 0;
            for(Pawn pawn : enemyPawns) {
                if(pawn.rank == this.rank)
                    c++;
            }
            if(c > 3)
                return rookBonus + 1.0f;
            else if(c > 2)
                return rookBonus + 0.8f;
            else if(c > 1)
                return rookBonus + 0.6f;
            else if(c > 0)
                return rookBonus + 0.3f;
            else {
                for(Pawn pawn : enemyPawns) {
                    if(pawn.file == file)
                        return 0.2f;
                }
                for(Pawn pawn : friendlyPawns) {
                    if(pawn.file == file) {
                        return 0f;
                    }
                }
                return rookBonus;
            }
        }
        
        for(Piece piece : friendlyPieces)
        {
            if(piece != this  &&  (piece instanceof Queen  ||  piece instanceof Rook)
               &&  piece.file == this.file  &&  piece.isEyeing(pos, rank, file))
                rookBonus += 0.25;
        }
        
        int pawnRank = 0;
        for(Pawn pawn : friendlyPawns)
        {
            if(pawn.file == this.file  &&  pawn.relativeRank() < pawnRank) {
                pawnRank = pawn.relativeRank();
            }
        }
        if(pawnRank == 6) {
            return 1.5f;
        }
        if(pawnRank == 5) {
            return 1.0f;
        }
        if(pawnRank > 2) {
            return 0.25f;
        }
        if(pawnRank > 0) {
            return 0f;
        }
        
        int c = 0;
        for(Pawn pawn : enemyPawns)
        {
            if(pawn.file == this.file)
                c++;
        }
        if(c > 1)
            return rookBonus + 0.4f;
        if(c > 0)
            return rookBonus + 0.15f;
        
        return rookBonus + 0.6f;
    }
    
}
