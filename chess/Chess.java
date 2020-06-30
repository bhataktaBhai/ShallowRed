package chess;

import pieces.*;
import java.util.Scanner;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 * The only class with documentation
 * @author CyberRat
 * @version 2.0
 */
public class Chess
{
    /** The current state of the chessboard. */
    public Position position = null;

    /** A list of all Positions that ever occurred to check for
     * Draw by Threefold Repetition. */
    public ArrayList<Pair<Position, Integer>> allPositions = new ArrayList<>();

    /** Number of moves elapsed to check for the Fifty Move Rule. */
    public int numOfMoves = 0;


    /**
     * Sets up the board and maintains a list of all pieces.
     */
    public Chess()
    {
        ArrayList<Piece> pieces = new ArrayList<>(32);
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
                    pieces.add(piece);
                }
            }
        }
        position = new Position(pieces, 1, null);
        allPositions.add(new Pair(position, 1));
        numOfMoves = 0;
    }

    /**
     * Commences play; inputs moves in algebraic notation until a result is 
     * reached. Prints the board after each move and the result at the end.
     */
    public void play()
    {
        Scanner sc = new Scanner(System.in);
        int colour = 1;
        boolean checkmate, underCheck, stuck, unwinnable, fifty, threefold, end;
        checkmate = underCheck = stuck = unwinnable = fifty = threefold = end = false;
        System.out.println(position);
        
        while(!end)
        {
            System.out.println((colour == 1 ? "WHITE" : "BLACK") + " to move.");
            String input = sc.next();
            input = prep(input);
            Move move = move(colour, input);
            if(move == null)
                continue;
            
            colour *= -1;
            position = position.move(move);
            underCheck = position.CHECK;
            stuck = position.stuck();
            checkmate = underCheck  &&  stuck;
            unwinnable = !position.winnable();
            fifty = numOfMoves > 99;
            threefold = threefoldRepetition();
            end = stuck  ||  unwinnable  ||  fifty  ||  threefold;
            System.out.println(position);
            System.out.println(Utils.getCapturedPieces(position.pieces).toString());
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
    public Move move(int colour, String input)
    {
        King king = position.king;
        int len = input.length();
        
        //for when the user enters only one of destination co-ordinates.
        if(len < 3)
            return null;
        
        // "P optionalFile optionalRank destFile destRank promotionPiece"
        boolean pawnPromotion = input.matches("P[a-h]?[2-7]?[a-h][18][NBRQ]");
        // "Piece optionalFile optionalRank destFile destRank"
        boolean normalMove = input.matches("[NBRQK][a-h]?[1-8]?[a-h][1-8]")  ||  input.matches("P[a-h]?[2-7]?[a-h][2-7]");
        // "0-0" | "0-0-0" | "O-O" | "O-O-O"
        boolean castling = input.matches("(0-0(-0)?)|(O-O(-O)?)");
        
        if(normalMove  ||  pawnPromotion)
        {
            char typeOfPiece = input.charAt(0);
            if(typeOfPiece != 'K'  &&  position.CHECKERS.size() > 1)
            {
                System.out.println("Double check. Must move King.");
                return null;
            }
            
            int promotionPiece = 0;
            if(pawnPromotion)
            {
                switch(input.charAt(len - 1))
                {
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
                input = input.substring(0, --len);
            }

            int originFile = -1, originRank = -1;

            //len >= 3 (Piece, destRank, destFile are musts).
            //if len == 4, one extra piece of information (rank | file).
            //if len == 5, two extra pieces of information (rank & file).
            if(len == 4)
                if(input.charAt(1) >= 'a'  &&  input.charAt(1) <= 'h')
                    originFile = input.charAt(1) - 'a';
                else
                    originRank = input.charAt(1) - '1';
            else if(len == 5)
            {
                originFile = input.charAt(1) - 'a';
                originRank = input.charAt(2) - '1';
            }

            int destinRank = input.charAt(len - 1) - '1';
            int destinFile = input.charAt(len - 2) - 'a';
            Piece enemyPiece = position.board[destinRank][destinFile];

                
            //when enemyPiece is not really an enemy piece
            if(enemyPiece != null  &&  enemyPiece.colour == colour)
            {
                System.out.println("Cannot capture one's own piece.");
                return null;
            }

            ArrayList<Piece> worthyMovers = new ArrayList<>();
            for(Piece piece : position.pieces)
            {
                if(piece.colour == colour  &&  Utils.instanceOf(piece, typeOfPiece))
                {
                    if(originFile != -1  &&  originFile != piece.file)
                        continue;
                    if(originRank != -1  &&  originRank != piece.rank)
                        continue;
                    if(piece.canMoveTo(position, destinRank, destinFile))
                        worthyMovers.add(piece);
                }
            }

            if(worthyMovers.size() > 1)
            {
                System.out.println("Ambiguity: " + worthyMovers.size()
                        + " pieces of the same kind can perform the specified move.");
                return null;
            }
            else if(worthyMovers.isEmpty())
            {
                System.out.println("Illegal move.");
                System.out.println(position);
                return null;
            }

            Piece theMover = worthyMovers.get(0);
            
            if(enemyPiece != null  ||  theMover instanceof Pawn)
            {
                numOfMoves = 0;
                allPositions.clear();
            }
            else
                numOfMoves++;
            
            return new Move(theMover, destinRank, destinFile, promotionPiece);
        }


        if(castling)
            if(len == 3  &&  king.canCastleShort(position))
                return new Move(king, king.rank, 6, 0);
            else if(len == 5  &&  king.canCastleLong(position))
                return new Move(king, king.rank, 2, 0);
            else
                System.out.println("Castling illegal.");

        else
            System.out.println("Enter valid algebraic notation.");

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
            if(position.equals(pair.getKey())) {
                System.out.println("WOOHOO!");
                allPositions.remove(i);
                allPositions.add(new Pair<>(position, pair.getValue() + 1));
                return pair.getValue() > 1;
            }
        }
        allPositions.add(new Pair<>(position, 1));
        return false;
    }
}