package chess;

import pieces.Piece;

public class Move
{
    private final int ORIGIN;
    private final int DESTIN;
    
    Move(Piece p, int[] move)
    {
        this(p, move[0], move[1], move.length > 2 ? move[2] : 0);
    }
    Move(Piece p, int rank, int file, int promotion)
    {
        ORIGIN = (p.rank << 3) + (p.file);
        DESTIN = (rank << 6) + (file << 3) + (promotion);
    }
    
    public int originRank() { return ORIGIN >> 3; }
    public int originFile() { return ORIGIN & 0b111; }
    
    public int rank() { return DESTIN >> 6; }
    public int file() { return (DESTIN >> 3) & 0b111; }
    public int promotion() { return DESTIN & 0b111;}
    
    public boolean equals(Move m)
    {
        return (m == null ? false : this.ORIGIN == m.ORIGIN  &&  this.DESTIN == m.DESTIN); 
    }
    
    private char promotionChar()
    {
        switch(promotion())
        {
            case 1:
                return 'Q';
            case 2:
                return 'R';
            case 3:
                return 'B';
            case 4:
                return 'N';
            default:
                return '\u0000';
        }
    }
    public String toString(Piece[][] board)
    {
        Piece MOVER = board[originRank()][originFile()];
        return String.format("%s%c%d%c", MOVER.toString(), file() + 'a',
                                    rank() + 1, promotionChar());
    }
    @Override
    public String toString()
    {
        return String.format("%c%d%s%c%d%c", originFile() + 'a', originRank() + 1,
                        " --> ", file() + 'a', rank() + 1, promotionChar());
    }
}
