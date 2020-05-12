package chess;

import pieces.*;
import java.util.ArrayList;

public class Position
{
    public King king;
    public final ArrayList<Piece> pieces;
    public final Piece[][] board = new Piece[8][8];
    public final int turn;
    public final ArrayList<Piece> CHECKERS;
    public final boolean CHECK;
    public final boolean shortCastle;
    public final boolean longCastle;
    public final Pawn doubleMover;
    private final boolean enPassantPossible;
    private Float evaluation = null;
    private Boolean stuck;
    
    Position(ArrayList<Piece> pieces, int turn, Pawn doubleMover)
    {
        this.pieces = new ArrayList<>(pieces.size());
        this.turn = turn;
        this.doubleMover = doubleMover;
        for(Piece piece : pieces) {
            this.pieces.add(piece);
            board[piece.rank][piece.file] = piece;
            if(piece instanceof King  &&  piece.colour == turn)
                king = (King)piece;
        }
        
        if(doubleMover == null)
            enPassantPossible = false;
        else
        {
            Piece pawnSuspect1 = Utils.get(board, doubleMover.rank, doubleMover.file - 1);
            Piece pawnSuspect2 = Utils.get(board, doubleMover.rank, doubleMover.file + 1);
            enPassantPossible =
                    (pawnSuspect1 instanceof Pawn  &&  pawnSuspect1.colour == turn)
                ||  (pawnSuspect2 instanceof Pawn  &&  pawnSuspect2.colour == turn);
        }
        
        CHECKERS = attackers(king);
        CHECK = !CHECKERS.isEmpty();
        
        shortCastle = king.canCastleShort(this);
        longCastle = king.canCastleLong(this);
        
    }

    public Piece nearestPieceFrom(int rank, int file, int rJump, int fJump)
    {
        int r = rank;
        int f = file;
        while(Utils.exists(r += rJump, f += fJump))
            if(board[r][f] != null)
                return board[r][f];
        return null;
    }
    /**
     * Returns a list of all the enemy Pieces attacking the given
     * Piece.
     * Note: Whether an attacker can actually capture the piece w/o
     * exposing his own king is not taken into account.
     * @param defender A {@code Piece} whose attackers are required.
     * @return An {@code ArrayList<Piece>} of all the attacker pieces.
     */
    public final ArrayList<Piece> attackers(Piece defender)
    {
        ArrayList<Piece> attackers = new ArrayList<>();
        
        for(Piece attacker : pieces)
            if(attacker.colour != defender.colour)
                if(attacker.isEyeing(this, defender.rank, defender.file))
                    attackers.add(attacker);
        return attackers;
    }
    public boolean underAttack(int rank, int file)
    {
        for(Piece attacker : pieces)
            if(attacker instanceof Knight  &&  attacker.colour != turn)
                if(attacker.isEyeing(this, rank, file))
                    return true;
        for(int[] step : Piece.ALL_DIRECTIONS)
        {
            Piece attacker = nearestPieceFrom(rank, file, step[0], step[1]);
            if(attacker != null  &&  attacker.colour != turn)
            {
                if(attacker.mightBeEyeing(rank, file))
                    return true;
            }
        }
        return false;
    }
    
    
    /**
     * Checks if any piece of the player to move has any legal move.
     * Note: Does not check for stalemate. Returns true even in the
     * case of checkmate.
     * @return Whether the player has any legal move.
     */
    public boolean stuck()
    {
        if(stuck != null)
            return stuck;
        stuck = true;
        for(Piece piece : pieces) {
            if(piece.colour == turn) {
                if(!piece.movableTo(this).isEmpty()) {
                    stuck = false;
                    return stuck;
                }
            }
        }
        return stuck;
    }

    /**
     * Checks for draw by insufficient material.
     * @return Whether either player can checkmate the other
     * through any series of legal moves.
     */
    public boolean winnable()
    {
        //just the two kings. No winning.
        if(pieces.size() < 3) {
            return false;
        }
        
        //at least one side has at least 2 pieces (other than king).
        //Therefore, win possible.
        if(pieces.size() > 4) {
            return true;
        }

        Piece whitePiece = null, blackPiece = null;
        for(Piece piece : pieces) {
            if(piece instanceof Pawn  ||  piece instanceof Rook  ||  piece instanceof Queen) {
                return true;
            }
            if(piece instanceof Knight  ||  piece instanceof Bishop) {
                if(piece.colour == 1) {
                    whitePiece = piece;
                }
                else {
                    blackPiece = piece;
                }
            }
        } //only minor pieces (N or B) left if made it to this point.

        //only one minor piece cannot win.
        if(pieces.size() == 3) {
            return false;
        } //exactly 4 pieces left if made it to this point.

        //one still remains null means the other side has 2 minors. Win-win.
        //not really necessary to check, but helps keep logic.
        if(whitePiece == null  ||  blackPiece == null) {
            return true;
        }

        //the very special case of two bishops not on the same colour.
        //In |kbB5|8|K7|8|8|8|8|8|, for instance, Bb7# is checkmate.
        //When the bishops are on the same colour, no checkmate possible.
        if(whitePiece instanceof Bishop  &&  blackPiece instanceof Bishop) {
            int whiteBishopSquareColour = (whitePiece.rank + whitePiece.file) % 2;
            int blackBishopSquareColour = (blackPiece.rank + blackPiece.file) % 2;
            return whiteBishopSquareColour != blackBishopSquareColour;
        }
        
        return true;
    }

    
    @Override
    public boolean equals(Object o)
    {
        return o instanceof Position ? equals((Position)o) : false;
    }
    public boolean equals(Position pos)
    {
        return (turn == pos.turn  &&  shortCastle == pos.shortCastle  &&  longCastle == pos.longCastle
                &&  enPassantPossible == pos.enPassantPossible  &&  boardEquals(pos.board));
    }
    private boolean boardEquals(Piece[][] board)
    {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(this.board[i][j] != board[i][j]) {
                    if(this.board[i][j] == null  ||  board[i][j] == null)
                        return false;
                    if(this.board[i][j].symbol != board[i][j].symbol)
                        return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        String s = "";
        for(int i = 0; i < 8; i++)
        {
            s += ((8 - i) + "|");
            for(int j = 0; j < 8; j++) {
                s = s + ((board[7 - i][j] == null ? ' ' : board[7 - i][j]) + "|");
            }
            s += ("\t\t" + (i + 1) + "|");
            for(int j = 0; j < 8; j++) {
                s = s + ((board[i][7 - j] == null ? ' ' : board[i][7 - j]) + "|");
            }
            s += '\n';
        }
        s += ("  a b c d e f g h \t\t  h g f e d c b a\n");
        s += String.valueOf(doubleMover);
        return s;
    }
    
    public Position move(Move move)
    {
        Piece mover = this.board[move.originRank()][move.originFile()];
        int rank = move.rank();
        int file = move.file();
        
        ArrayList<Piece> newPieces = (ArrayList<Piece>) pieces.clone();
        Pawn doubleMover = null;
        
        for(int i = 0; i < newPieces.size(); i++) {
            Piece piece = newPieces.get(i);
            if(piece.equals(mover)) {
                newPieces.remove(i);
                
                //capture...
                if(board[rank][file] != null) {
                    newPieces.remove(board[rank][file]);
                }
                //castling...
                else if(piece instanceof King  &&  Math.abs(piece.file - file) == 2) {
                    Piece rook = board[piece.rank][file == 2 ? 0 : 7];
                    newPieces.remove(rook);
                    newPieces.add(rook.move(piece.rank, file == 2 ? 3 : 5));
                }
                //en Passant...
                else if(piece instanceof Pawn  &&  file != piece.file  &&  board[rank][file] == null) {
                    newPieces.remove(board[piece.rank][file]);
                }
                
                Piece newPiece = piece.move(move);
                newPieces.add(newPiece);
                
                if(piece instanceof Pawn  &&  Math.abs(rank - piece.rank) == 2)
                    doubleMover = (Pawn) newPiece;
                
                return new Position(newPieces, -turn, doubleMover);
            }
        }
        return null;
    }
    
    public float eval() {
        if(evaluation != null) {
            return evaluation;
        }
        if(CHECK  &&  stuck()) {
            return 100000;
        }
        ArrayList<Piece> whitePieces = new ArrayList<>();
        ArrayList<Piece> blackPieces = new ArrayList<>();
        ArrayList<Pawn> whitePawns = new ArrayList<>();
        ArrayList<Pawn> blackPawns = new ArrayList<>();
        for(Piece piece : pieces) {
            if(piece.colour == turn) {
                if(piece instanceof Pawn) {
                    whitePawns.add((Pawn)piece);
                }
                else {
                    whitePieces.add(piece);
                }
            }
            else {
                if(piece instanceof Pawn) {
                    blackPawns.add((Pawn)piece);
                }
                else {
                    blackPieces.add(piece);
                }
            }
        }
        float whitePoints = 0f, blackPoints = 0f;
        for(Pawn pawn : whitePawns) {
            whitePoints += pawn.value() + pawn.developmentPoints();
            if(pawn.isDoubled(whitePawns)) {
                whitePoints -= 0.25f;
            }
            if(pawn.isIsolated(whitePawns)) {
                whitePoints -= pawn.file % 7 == 0 ? 0.12f : 0.25f;
            }
            else if(pawn.isBackward(whitePawns)) {
                whitePoints -= pawn.file % 7 == 0 ? 0.05f : 0.1f;
            }
        }
        for(Pawn pawn : blackPawns) {
            blackPoints += pawn.value() + pawn.developmentPoints();
            if(pawn.isDoubled(blackPawns)) {
                blackPoints -= 0.25f;
            }
            if(pawn.isIsolated(blackPawns)) {
                blackPoints -= 0.25f;
            }
            else if(pawn.isBackward(blackPawns)) {
                blackPoints -= 0.1f;
            }
        }
        
        for(Piece piece : whitePieces) {
            whitePoints += piece.value() 
            + piece.developmentPoints(this, whitePawns, blackPawns, whitePieces, blackPieces);
        }
        for(Piece piece : blackPieces) {
            blackPoints += piece.value()
            + piece.developmentPoints(this, blackPawns, whitePawns, blackPieces, whitePieces);
        }
        evaluation = blackPoints - whitePoints;
        return evaluation;
    }
    
}
