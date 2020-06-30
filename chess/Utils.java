package chess;

import java.util.ArrayList;
import java.util.Arrays;
import pieces.*;

public abstract class Utils
{
    /**
     * A 'safe' way to access a member of a chessboard. Avoids
     * <code>ArrayIndexOutOfBoundsException</code>.
     * <p>
     * Identical to <code>{@link #exists(int, int) exists(rank, file)} ?
     * board[rank][file] : null;</code>
     * </p>
     * 
     * @param board     A 2D Piece array representing the chessboard
     * @param rank      The required rank
     * @param file      The required file
     * 
     * @return          board[rank][file], or null in the case of non-existent
     *                  co-ordinates
     * @see             exists(int, int) 
     */
    public static Piece get(Piece[][] board, int rank, int file)
    {
        return exists(rank, file) ? board[rank][file] : null;
    }
    
    public static Piece[][] getBoard(ArrayList<Piece> pieces)
    {
        Piece[][] board = new Piece[8][8];
        for(Piece piece : pieces) {
            board[piece.rank][piece.file] = piece;
        }
        return board;
    }

    public static boolean exists(int rank, int file)
    {
        return rank >>> 3 == 0  &&  file >>> 3 == 0;
    }
    public static boolean instanceOf(Piece piece, char c)
    {
        switch(c) {
            case 'B':
                return (piece instanceof Bishop);
            case 'K':
                return (piece instanceof King);
            case 'N':
                return (piece instanceof Knight);
            case 'P':
                return (piece instanceof Pawn);
            case 'Q':
                return (piece instanceof Queen);
            case 'R':
                return (piece instanceof Rook);
        }
        return false;
    }
    
    public static ArrayList<int[]> squaresBetween(Piece p, Piece q)
    {
        return squaresBetween(p.rank, p.file, q.rank, q.file);
    }
    public static ArrayList<int[]> squaresBetween(int r1, int f1, int r2, int f2)
    {
        ArrayList<int[]> squares = new ArrayList<>();
        int rJump = (int) Math.signum(r2 - r1);
        int fJump = (int) Math.signum(f2 - f1);
        while((r1 += rJump) != r2  |  (f1 += fJump) != f2)
            squares.add(new int[]{r1, f1});
        return squares;
    }

    /**
     * Prints the chessboard from both players' sides. Also prints the rank and
     * file names next to the ranks and files to make it easier to use
     * algebraic notation. This is entirely text-based and the board, alas, is
     * not black and white. Just white.
     * 
     * @param board     A 2D Piece array representing the chessboard
     */
    public static void print(Piece[][] board)
    {
        for(int i = 0; i < 8; i++)
        {
            System.out.print((8 - i) + "|");
            for(int j = 0; j < 8; j++) {
                System.out.print((board[7 - i][j] == null ? ' ' : board[7 - i][j]) + "|");
            }
            System.out.print("\t\t" + (i + 1) + "|");
            for(int j = 0; j < 8; j++) {
                System.out.print((board[i][7 - j] == null ? ' ' : board[i][7 - j]) + "|");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h \t\t  h g f e d c b a ");
    }
    
    public static void printMove(int[] move)
    {
        if(move.length > 2) {
            char promotion = '\u0000';
            switch(move[2]) {
                case 1:
                    promotion = 'Q';
                    break;
                case 2:
                    promotion = 'R';
                    break;
                case 3:
                    promotion = 'B';
                    break;
                case 4:
                    promotion = 'N';
                    break;
            }
            System.out.println(Integer.toString(move[1] + 'a') + (move[0] + 1) + (promotion));
        }
        else {
            System.out.println(Integer.toString(move[1] + 'a') + (move[0] + 1));
        }
    }
    
    public static boolean isEyedUpon(Position pos, int rank, int file)
    {
        Piece attacker;
        for(int[] step : Piece.ALL_DIRECTIONS) {
            int r = rank + step[0];
            int f = file + step[1];
            while(exists(r, f)) {
                attacker = pos.board[r][f];
                if(attacker != null) {
                    if(attacker.colour != pos.turn  &&  attacker.mightBeEyeing(rank, file))
                        return true;
                    break;
                }
            }
        }
        for(Piece knight : pos.pieces) {
            if(knight instanceof pieces.Knight  &&  knight.colour != pos.turn) {
                if(knight.mightBeEyeing(rank, file))
                    return true;
            }
        }
        return false;
    }
    
    public static ArrayList<Character> getCapturedPieces(ArrayList<Piece> boardPieces)
    {
        ArrayList<Character> capturedPieces = new ArrayList<>
        (Arrays.asList(new Character[]{'♔','♕','♖','♖','♗','♗','♘','♘','♙','♙','♙','♙','♙','♙','♙','♙',
                                       '♟','♟','♟','♟','♟','♟','♟','♟','♞','♞','♝','♝','♜','♜','♛','♚'}));
        for(Piece piece : boardPieces)
            capturedPieces.remove((Character)piece.symbol);
        return capturedPieces;
    }
    /*public static ArrayList<Piece> deepCopy(ArrayList<Piece> pieces)
    {
        ArrayList<Piece> copy = new ArrayList<>();
        for(Piece piece : pieces) {
            copy.add(piece.clone());
        }
        return copy;
    }*/
}
