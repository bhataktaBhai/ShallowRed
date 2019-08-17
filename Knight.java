package pieces;

import chess.Utils;
import chess.Position;
import java.util.ArrayList;

public class Knight extends Piece
{
    private static final int VAL = 3;
    private static final boolean FREELY_MOVING = false;
    private static final int[][] STEPS = {{+1,+2},{+2,+1},{+2,-1},{+1,-2},{-1,-2},{-2,-1},{-2,+1},{-1,+2}};
    
    Knight(char symbol, int rank, int file)
    {
        super(symbol, rank, file);
    }

    @Override
    public ArrayList<int[]> eyeing(Position pos)
    {
        ArrayList<int[]> eye = new ArrayList<>();
        for(int[] step : STEPS) {
            int[] move = {rank + step[0], file + step[1]};
            if(Utils.exists(move[0], move[1])) {
                eye.add(move);
            }
        }
        return eye;
    }
    @Override
    public ArrayList<Piece> eyeingEnemies(Position pos)
    {
        ArrayList<Piece> eye = new ArrayList<>();
        for(int[] step : STEPS) {
            if(Utils.exists(rank + step[0], file + step[1])) {
                Piece enemy = pos.board[rank + step[0]][file + step[1]];
                if(enemy != null  &&  enemy.colour != colour)
                    eye.add(enemy);
            }
        }
        return eye;
    }
    
    //@Override
    public boolean canMoveInDirection(int rJump, int fJump)
    {
        if(Math.abs(rJump) == 1)
            return Math.abs(fJump) == 2;
        if(Math.abs(fJump) == 1)
            return Math.abs(rJump) == 2;
        return false;
    }
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
        
        int rJump = Math.abs(rank - this.rank);
        int fJump = Math.abs(file - this.file);
        return (rJump == 2  &&  fJump == 1  ||  rJump == 1  &&  fJump == 2);
    }
    @Override
    public boolean isEyeing(Position notRequired, int rank, int file)
    {
        return mightBeEyeing(rank, file);
    }
    
    @Override
    public ArrayList<int[]> movableTo(Position pos)
    {
        King king = pos.king;
        ArrayList<int[]> legalMoves = new ArrayList<>();
        
        if(pinned(pos)) {
            return legalMoves;
        }
        
        if(pos.CHECK) {
            if(pos.CHECKERS.size() > 1) {
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
            if(this.isEyeing(pos, checker.rank, checker.file)) {
                legalMoves.add(new int[]{checker.rank, checker.file});
            }
            
            return legalMoves;
        }
        
        for(int[] step : STEPS) {
            int[] move = {rank + step[0], file + step[1]};
            if(Utils.exists(move[0], move[1])) {
                Piece moveSuspect = pos.board[move[0]][move[1]];
                if((moveSuspect == null)  ||  (moveSuspect.colour != colour)) {
                    legalMoves.add(move);
                }
            }
        }
        
        return legalMoves;
    }
    
    boolean isPawnported(ArrayList<Pawn> friendlyPawns)
    {
        for(Pawn pawn : friendlyPawns) {
            if(pawn.relativeRank() == this.relativeRank() - 1  &&  (pawn.file == file - 1  ||  pawn.file == file + 1)) {
                return true;
            }
        }
        return false;
    }
    int outpostStatus(ArrayList<Pawn> enemyPawns)
    {
        int laGuardia = 1;
        for(Pawn pawn : enemyPawns) {
            if(pawn.file == file + 1  ||  pawn.file == file - 1) {
                if(colour * pawn.rank > colour * rank) {
                    return 0;
                }
            }
            if(pawn.file == file  &&  colour * pawn.rank > colour * rank) {
                laGuardia = 2;
            }
        }
        return laGuardia;
    }
    
    @Override
    public float developmentPoints(Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns,
                                    ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces)
    {
        int outpostStatus = outpostStatus(enemyPawns);
        boolean isPawnported = isPawnported(friendlyPawns);
        switch (relativeRank()) {
            case 0:
                return 0f;
            case 1:
                return 0.1f;
            case 2:
                if(file > 0  &&  file < 7) {
                    return 0.2f;
                }
                else {
                    return 0.13f;
                }
            case 3:
            case 4:
                if(outpostStatus > 0) {
                    if(file > 0  &&  file < 7) {
                        return 0.3f;
                    }
                    else {
                        return 0.18f;
                    }
                }
                else if(file > 0  &&  file < 7) {
                    return 0.22f;
                }
                else {
                    return 0.15f;
                }
            case 5:
            case 6:
                if(isPawnported) {
                    if(outpostStatus > 1) {
                        return 1f;
                    }
                    else if(outpostStatus > 0){
                        return 0.8f;
                    }
                    else {
                        return 0.5f;
                    }
                }
                else if(outpostStatus > 1) {
                    return 0.75f;
                }
                else if(outpostStatus > 0) {
                    return 0.55f;
                }
                else {
                    return 0.3f;
                }
            default:
                return 0.3f;
        }
    }
    
    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return STEPS; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }
    
}