package chess;

import except.NoKingException;
import except.NullPieceException;
import pieces.King;
import pieces.Pawn;
import pieces.Piece;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.util.Pair;

/**
 * The only class with documentation
 * @author CyberRat
 * @version 2.0
 */
public class Chess
{
    /** The current state of the chessboard. */
    public Position currentPos = null;
    
    /** A 2D Array, of {@code Piece} objects, the size of a chessboard. */
    public Piece[][] board = new Piece[8][8];

    /** A list of all the Pieces on the chessboard. */
    public ArrayList<Piece> allPieces = new ArrayList<>();


    /** A list of all Positions that ever occurred to check for
     * Draw by Threefold Repetition. */
    public ArrayList<Pair<Position, Integer>> allPositions = new ArrayList<>();

    private King whiteKing = null;
    private King blackKing = null;
    private Pawn doubleMover;

    /** Number of moves elapsed to check for the Fifty Move Rule. */
    public int numOfMoves = 0;


    /**
     * Sets up the board and maintains a list of all pieces.
     */
    public Chess() throws NullPieceException, NoKingException
    {
        char[][] tempBoard =
        {{'♖','♘','♗','♕','♔','♗','♘','♖'},
         {'♙','♙','♙','♙','♙','♙','♙','♙'},
         {' ',' ',' ',' ',' ',' ',' ',' '},
         {' ',' ',' ',' ',' ',' ',' ',' '},
         {' ',' ',' ',' ',' ',' ',' ',' '},
         {' ',' ',' ',' ',' ',' ',' ',' '},
         {'♟','♟','♟','♟','♟','♟','♟','♟'},
         {'♜','♞','♝','♛','♚','♝','♞','♜'}};
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(tempBoard[i][j] != ' ') {
                    Piece piece = Piece.getPiece(tempBoard[i][j], i, j, false);
                    if(piece instanceof King) {
                        if(piece.colour == 1) {
                            whiteKing = (King)piece;
                        }
                        else {
                            blackKing = (King)piece;
                        }
                    }
                    allPieces.add(piece);
                }
            }
        }
        currentPos = new Position(allPieces, 1, null);
        board = currentPos.board;
        allPositions.add(new Pair(currentPos, 1));
        numOfMoves = 0;
    }

    /**
     * Commences play; inputs moves in algebraic notation until a result is 
     * reached. Prints the board after each move and the result at the end.
     */
    public void play() throws NullPieceException, NoKingException
    {
        Scanner sc = new Scanner(System.in);
        int colour = 1;
        boolean checkmate, underCheck, stuck, unwinnable, fifty, threefold, end;
        checkmate = underCheck = stuck = unwinnable = fifty = threefold = end = false;
        Utils.print(board);
        
        while(!end)
        {
            System.out.println((colour == 1 ? "WHITE" : "BLACK") + " to move.");
            String input = sc.next();
            input = prep(input);
            if(move(colour, input) == null) {
                continue;
            }
            colour *= -1;
            currentPos = new Position(allPieces, colour, doubleMover);
            board = Utils.getBoard(allPieces);
            underCheck = currentPos.CHECK;
            stuck = currentPos.stuck();
            checkmate = underCheck  &&  stuck;
            unwinnable = !currentPos.winnable();
            fifty = numOfMoves > 99;
            threefold = threefoldRepetition();
            end = stuck  ||  unwinnable  ||  fifty  ||  threefold;
            Utils.print(board);
            System.out.println(Utils.getCapturedPieces(currentPos.allPieces).toString());
            if(underCheck) {
                System.out.println("CHECK!");
            }
        }
        
        if(checkmate) {
            System.out.println("And MATE!");
            System.out.println(colour == 1 ? "0-1" : "1-0");
        }
        else {
            if(stuck) {
                System.out.println("Stalemate.");
            }
            if(unwinnable) {
                System.out.println("Insufficient material.");
            }
            if(fifty) {
                System.out.println("Fifty Moves w/o Pawn Move or Capture");
            }
            if(threefold) {
                System.out.println("Threefold Repetition.");
            }
            System.out.println("½ - ½");
        }
    }
    
    public void play2() throws NullPieceException, NoKingException
    {
        Scanner sc = new Scanner(System.in);
        int colour = 1;
        boolean checkmate, check, stuck, unwinnable, fifty, threefold, end;
        checkmate = check = stuck = unwinnable = fifty = threefold = end = false;
        Engine engine = new Engine();
        Utils.print(board);
        
        while(!end)
        {
            System.out.println((colour == 1 ? "WHITE" : "BLACK") + " to move.");
            
            if(colour == -1) {
                String input = sc.next();
                input = prep(input);
                if(move(colour, input) == null) {
                    continue;
                }
                currentPos = new Position(allPieces, -colour, doubleMover);
            }
            
            else {
                System.out.println("Let the engine move.");
                currentPos = engine.play(currentPos);
                allPieces.clear();
                for(Piece piece : currentPos.allPieces) {
                    allPieces.add(piece);
                    if(piece instanceof King) {
                        if(piece.colour == 1) {
                            whiteKing = (King)piece;
                        }
                        else {
                            blackKing = (King)piece;
                        }
                    }
                }
            }
            
            board = Utils.getBoard(allPieces);
            colour *= -1;
            check = currentPos.CHECK;
            stuck = currentPos.stuck();
            checkmate = check  &&  stuck;
            unwinnable = !currentPos.winnable();
            fifty = numOfMoves > 99;
            threefold = threefoldRepetition();
            end = stuck  ||  unwinnable  ||  fifty  ||  threefold;
            Utils.print(board);
            System.out.println(Utils.getCapturedPieces(currentPos.allPieces).toString());
            if(check) {
                System.out.println("CHECK!");
            }
        }
        
        if(checkmate) {
            System.out.println("And MATE!");
            System.out.println(colour == 1 ? "0-1" : "1-0");
        }
        else {
            if(stuck) {
                System.out.println("Stalemate.");
            }
            if(unwinnable) {
                System.out.println("Insufficient material.");
            }
            if(fifty) {
                System.out.println("Fifty Moves w/o Pawn Move or Capture");
            }
            if(threefold) {
                System.out.println("Threefold Repetition.");
            }
            System.out.println("½ - ½");
        }
    }
    
    public void play3() throws NullPieceException, NoKingException, Exception
    {
        Scanner sc = new Scanner(System.in);
        int colour = 1;
        boolean checkmate, check, stuck, unwinnable, fifty, threefold, end;
        checkmate = check = stuck = unwinnable = fifty = threefold = end = false;
        Engine2 engine = new Engine2();
        Utils.print(board);
        
        Move lastMove = null;
        while(!end)
        {
            System.out.println((colour == 1 ? "WHITE" : "BLACK") + " to move.");
            
            if(colour == -1) {
                String input = sc.next();
                input = prep(input);
                lastMove = move(colour, input);
                if(lastMove == null) {
                    continue;
                }
                currentPos = new Position(allPieces, -colour, doubleMover);
            }
            
            else {
                System.out.println("Let the engine move.");
                Move move = engine.play(currentPos, lastMove);
                System.out.println(move.toString(board));
                currentPos = currentPos.move(move);
                allPieces.clear();
                for(Piece piece : currentPos.allPieces) {
                    allPieces.add(piece);
                    if(piece instanceof King) {
                        if(piece.colour == 1) {
                            whiteKing = (King)piece;
                        }
                        else {
                            blackKing = (King)piece;
                        }
                    }
                }
            }
            
            board = Utils.getBoard(allPieces);
            colour *= -1;
            check = currentPos.CHECK;
            stuck = currentPos.stuck();
            checkmate = check  &&  stuck;
            unwinnable = !currentPos.winnable();
            fifty = numOfMoves > 99;
            threefold = threefoldRepetition();
            end = stuck  ||  unwinnable  ||  fifty  ||  threefold;
            Utils.print(board);
            System.out.println(Utils.getCapturedPieces(currentPos.allPieces).toString());
            System.out.printf("%.2f\n", currentPos.eval());
            if(check) {
                System.out.println("CHECK!");
            }
        }
        
        if(checkmate) {
            System.out.println("And MATE!");
            System.out.println(colour == 1 ? "0-1" : "1-0");
        }
        else {
            if(stuck) {
                System.out.println("Stalemate.");
            }
            if(unwinnable) {
                System.out.println("Insufficient material.");
            }
            if(fifty) {
                System.out.println("Fifty Moves w/o Pawn Move or Capture");
            }
            if(threefold) {
                System.out.println("Threefold Repetition.");
            }
            System.out.println("½ - ½");
        }
    }

    /**
     * Prepares the input move to be parsed. Removes optional annotations,
     * such as {@code "x","+","++","#","!","?","?!","!?"," e.p."}, and prefixes
     * it with a {@code "P"} in case of a Pawn move.
     * @param move A move in algebraic notation.
     * @return The move in a notation reduced to the essentials.
     */
    public String prep(String move)
    {
        while(true) {
            int x = move.indexOf('x');
            if(x < 0) {
                break;
            }
            move = move.substring(0, x) + move.substring(x + 1);
        }
        if(Character.isLowerCase(move.charAt(0))) {
            move = 'P' + move;
        }
        while(!move.substring(move.length() - 1).matches("[0-8QRBNO]")) {
            move = move.substring(0, move.length() - 1);
        }
        return move;
    }

    /**
     * Performs the given move if it is valid and legal.
     * @param colour The player to move (1 = White, -1 = Black).
     * @param input The move in formatted algebraic notation, as by prep().
     * @return A Move object of the move played.
     */
    public Move move(int colour, String input) throws NullPieceException
    {
        King king = colour == 1 ? whiteKing : blackKing;
        int len = input.length();
        doubleMover = null;
        
        //for when the user enters only one of destination co-ordinates.
        if(len < 3) {
            return null;
        }
        
        // "P optionalFile optionalRank destFile destRank promotionPiece"
        boolean pawnPromotion = input.matches("P[a-h]?[2-7]?[a-h][18][NBRQ]");
        // "Piece optionalFile optionalRank destFile destRank"
        boolean normalMove = input.matches("[NBRQK][a-h]?[1-8]?[a-h][1-8]")  ||  input.matches("P[a-h]?[2-7]?[a-h][2-7]");
        // "0-0" | "0-0-0" | "O-O" | "O-O-O"
        boolean castling = input.matches("(0-0(-0)?)|(O-O(-O)?)");
        
        if(normalMove  ||  pawnPromotion) {
            char typeOfPiece = input.charAt(0);
            int promotionPiece = 0;
            if(pawnPromotion) {
                switch(input.charAt(--len)) {
                    case 'Q':
                        promotionPiece = 1;
                        break;
                    case 'R':
                        promotionPiece = 2;
                        break;
                    case 'B':
                        promotionPiece = 3;
                        break;
                    case 'N':
                        promotionPiece = 4;
                        break;
                }
                input = input.substring(0, len);
            }

            int originalFile = -1, originalRank = -1;

            //len >= 3 (Piece, destRank, destFile are musts).
            //if len == 4, one extra piece of information (rank | file).
            //if len == 5, two extra pieces of information (rank & file).
            if(len == 4) {
                if(input.charAt(1) >= 'a'  &&  input.charAt(1) <= 'h') {
                    originalFile = input.charAt(1) - 'a';
                }
                else {
                    originalRank = input.charAt(1) - '1';
                }
            }
            else if(len == 5) {
                originalFile = input.charAt(1) - 'a';
                originalRank = input.charAt(2) - '1';
            }

            int destRank = input.charAt(len - 1) - '1';
            int destFile = input.charAt(len - 2) - 'a';
            Piece enemyPiece = board[destRank][destFile];

            if(typeOfPiece != 'K'  &&  currentPos.CHECKERS.size() > 1) {
                System.out.println("Double check. Must move King.");
                return null;
            }
                
            //enemyPiece is not really an enemy piece. Betrayal ain't a thing.
            if(enemyPiece != null  &&  enemyPiece.colour == colour) {
                System.out.println("Cannot capture one's own piece.");
                return null;
            }

            //System.out.println("" + (char)(destFile + 'a') + (destRank + 1));
            ArrayList<Piece> worthyMovers = new ArrayList<>();
            for(Piece piece : allPieces) {
                if(piece.colour == colour  &&  piece.instanceOf(typeOfPiece)) {
                    if(originalFile != -1  &&  originalFile != piece.file) {
                        continue;
                    }
                    if(originalRank != -1  &&  originalRank != piece.rank) {
                        continue;
                    }
                    if(piece.canMoveTo(currentPos, destRank, destFile)) {
                        if(piece instanceof Pawn  &&  destFile != piece.file  &&  enemyPiece == null) {
                            enemyPiece = board[piece.rank][destFile];
                        }
                        
                        worthyMovers.add(piece);
                    }
                }
            }

            if(worthyMovers.size() > 1) {
                System.out.println("Ambiguity: " + worthyMovers.size() + " pieces of the same kind can perform the specified move.");
                return null;
            }
            else if(worthyMovers.isEmpty()) {
                System.out.println("Illegal move.");
                System.out.println(currentPos);
                return null;
            }

            Piece theMover = worthyMovers.get(0);
            allPieces.remove(theMover);
            
            int[] move;
            if(promotionPiece != 0)
                move = new int[]{destRank, destFile, promotionPiece};
            else
                move = new int[]{destRank, destFile};
            
            allPieces.add(theMover.move(move));
            if(enemyPiece != null) {
                allPieces.remove(enemyPiece);
                //for 50 Move and 3x Rep.
                numOfMoves = 0;
                allPositions.clear();
            }
            
            if(theMover instanceof Pawn) {
                numOfMoves = 0;
                allPositions.clear();
                if(Math.abs(destRank - theMover.rank) == 2) {
                    doubleMover = (Pawn) theMover;
                }
            }
            else {
                numOfMoves++;
            }
            
            return new Move(theMover, move);
        }


        if(castling) {
            if(len == 3  &&  king.canCastleShort(currentPos)) {
                allPieces.remove(king);
                int[] move = {king.rank, 6};
                king = (King) king.move(move);
                allPieces.add(king);
                if(king.colour == 1)
                    whiteKing = king;
                else 
                    blackKing = king;
                for(Piece piece : allPieces) {
                    if(piece.rank == king.rank  &&  piece.file == 7) {
                        allPieces.remove(piece);
                        allPieces.add(piece.move(new int[]{piece.rank, 5}));
                        break;
                    }
                }
                return new Move(king, move);
            }
            if(len == 5  &&  king.canCastleLong(currentPos)) {
                allPieces.remove(king);
                int[] move = {king.rank, 2};
                king = (King) king.move(move);
                allPieces.add(king);
                if(king.colour == 1)
                    whiteKing = king;
                else 
                    blackKing = king;
                for(Piece piece : allPieces) {
                    if(piece.rank == king.rank  &&  piece.file == 0) {
                        allPieces.remove(piece);
                        allPieces.add(piece.move(new int[]{piece.rank, 3}));
                        break;
                    }
                }
                return new Move(king, move);
            }
            System.out.println("Castling illegal.");
        }

        else {
            System.out.println("Enter valid algebraic notation.");
        }

        return null;
    }

    /**
     * Checks for draw by threefold repetition.
     * @return Whether the current position has been repeated thrice.
     */
    public boolean threefoldRepetition()
    {
        for(int i = 0; i < allPositions.size(); i++) {
            Pair<Position, Integer> pair = allPositions.get(i);
            if(currentPos.equals(pair.getKey())) {
                System.out.println("WOOHOO!");
                allPositions.remove(i);
                allPositions.add(new Pair<>(currentPos, pair.getValue() + 1));
                return pair.getValue() > 1;
            }
        }
        allPositions.add(new Pair<>(currentPos, 1));
        return false;
    }
}