package pieces;

import chess.Utils;
import chess.Position;
import java.util.ArrayList;

public class King extends Piece
{
    private static final int VAL = Integer.MAX_VALUE;
    private static final boolean FREELY_MOVING = false;
    private final boolean HAS_MOVED;

    public King(char symbol, int rank, int file, boolean hasMoved)
    {
        super(symbol, rank, file);
        this.HAS_MOVED = hasMoved;
    }

    @Override
    public ArrayList<int[]> eyeing(Position pos)
    {
        ArrayList<int[]> eye = new ArrayList<>();
        for(int[] step : ALL_DIRECTIONS) {
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
        for(int[] step : ALL_DIRECTIONS) {
            if(Utils.exists(rank + step[0], file + step[1])) {
                Piece enemy = pos.board[rank + step[0]][file + step[1]];
                if(enemy != null  &&  enemy.colour != colour)
                    eye.add(enemy);
            }
        }
        return eye;
    }

    public boolean canMoveInDirection(int rJump, int fJump)
    {
        rJump = Math.abs(rJump);
        fJump = Math.abs(fJump);
        return rJump < 2  &&  fJump < 2  &&  rJump + fJump != 0;
    }
    @Override
    public boolean mightBeEyeing(int rank, int file)
    {
        return isEyeing(null, rank, file);
    }
    @Override
    public boolean isEyeing(Position notRequired, int rank, int file)
    {
        if(rank == this.rank  &&  file == this.file)
            return false;
    
        int rJump = Math.abs(rank - this.rank);
        int fJump = Math.abs(file - this.file);
        return rJump < 2  &&  fJump < 2;
    }

    @Override
    public ArrayList<int[]> movableTo(Position pos)
    {
        ArrayList<int[]> legalMoves = new ArrayList<>();
        Integer skip_r_1 = null, skip_f_1 = null;
        Integer skip_r_2 = null, skip_f_2 = null;
        if(pos.CHECK) {
            Piece checker = pos.CHECKERS.get(0);
            if(checker.isFreelyMoving()) {
                skip_r_1 = (int) Math.signum(rank - checker.rank);
                skip_f_1 = (int) Math.signum(file - checker.file);
            }
            if(pos.CHECKERS.size() > 1) {
                checker = pos.CHECKERS.get(1);
                if(checker.isFreelyMoving()) {
                    skip_r_2 = (int) Math.signum(rank - checker.rank);
                    skip_f_2 = (int) Math.signum(file - checker.file);
                }
            }
        }
        
        outer:
        for(int[] step : ALL_DIRECTIONS) {
            if(skip_r_1 != null  &&  step[0] == skip_r_1  &&  step[1] == skip_f_1)
                continue;
            if(skip_r_2 != null  &&  step[0] == skip_r_2  &&  step[1] == skip_f_2)
                continue;
            int[] move = {rank + step[0], file + step[1]};
            if(Utils.exists(move[0], move[1])) {
                Piece moveSuspect = pos.board[move[0]][move[1]];
                if((moveSuspect == null)  ||  (moveSuspect.colour != colour)) {
                    for(int i = 0; i < pos.allPieces.size(); i++) {
                        Piece enemy = pos.allPieces.get(i);
                        if(enemy.colour != colour  &&  enemy.isEyeing(pos, move[0], move[1]))
                            continue outer;
                    }
                    legalMoves.add(move);
                }
            }
        }
        if(canCastleShort(pos)) {
            legalMoves.add(new int[]{rank, file + 2});
        }
        if(canCastleLong(pos)) {
            legalMoves.add(new int[]{rank, file - 2});
        }
        return legalMoves;
    }

    public boolean canCastleShort(Position pos)                            //Review.
    {
        if(HAS_MOVED) {
            return false;
        }
        if(pos.CHECK) {
            return false;
        }
        Piece rook = pos.board[rank][7];
        if(!(rook instanceof Rook)  ||  rook.hasMoved()) {
            return false;
        }
        if(pos.board[rank][5] != null  ||  pos.board[rank][6] != null) {
            return false;
        }
        int[][] squaresToTraverse = {{rank, 5},{rank, 6}};
        for(Piece enemy : pos.allPieces) {
            if(enemy.colour != colour) {
                for(int[] hostile : enemy.eyeing(pos)) {
                    for(int[] move : squaresToTraverse) {
                        if(hostile[0] == move[0]  &&  hostile[1] == move[1]) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public boolean canCastleLong(Position pos)                             //Review.
    {
        if(HAS_MOVED) {
            return false;
        }
        if(pos.CHECK) {
            return false;
        }
        Piece rook = pos.board[rank][0];
        if(!(rook instanceof Rook)  ||  rook.hasMoved()) {
            return false;
        }
        if(pos.board[rank][3] != null  ||  pos.board[rank][2] != null  ||  pos.board[rank][1] != null) {
            return false;
        }
        int[][] squaresToTraverse = {{rank, 3},{rank, 2}};
        for(Piece enemy : pos.allPieces) {
            if(enemy.colour != colour) {
                for(int[] hostile : enemy.eyeing(pos)) {
                    for(int[] move : squaresToTraverse) {
                        if(hostile[0] == move[0]  &&  hostile[1] == move[1]) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /*
    double fortressPoints(ArrayList<Pawn> friendlyPawns)
    {
        if(relativeRank > 2) {
            return 0;
        }
        int pawns = 0;
        double points1, points2 = 0;
        for(Pawn pawn : friendlyPawns) {
            if(pawn.rank == rank  ||  pawn.rank == rank + 1  ||  pawn.rank == rank - 1) {
                if(pawn.file == file  &&  pawn.relativeRank > relativeRank  ||  pawn.file == file + 1  ||  pawn.file == file - 1) {
                    pawns++;
                }
            }
            else if(pawn.relativeRank == relativeRank + 2  &&  pawn.file == file) {
                rank += colour;
                relativeRank ++;
                points2 = fortressPoints(friendlyPawns);
                rank -= colour;
                relativeRank --;
            }
        }
        if(relativeRank == 0) {
            if(file == 0  ||  file == 7) {
                points1 = pawns * 0.8;
            }
            else {
                points1 = pawns * 0.55;
            }
        }
        else {
            if(file == 0  ||  file == 7) {
                points1 = pawns * 0.8 - 0.8;
            }
            else {
                points1 = pawns * 0.55 - 0.55;
            }
        }
        return points1 > points2 - 0.5 ? points1 : points2 - 0.5;
    }
    */

    @Override
    public float value() { return VAL; }
    @Override
    public int[][] steps() { return ALL_DIRECTIONS; }
    @Override
    public boolean isFreelyMoving() { return FREELY_MOVING; }
    @Override
    public boolean hasMoved() { return HAS_MOVED; }

    @Override
    public float developmentPoints
        (Position pos, ArrayList<Pawn> friendlyPawns, ArrayList<Pawn> enemyPawns,
         ArrayList<Piece> friendlyPieces, ArrayList<Piece> enemyPieces)
    {
        return 0; //To change body of generated methods, choose Tools | Templates.
    }

}
