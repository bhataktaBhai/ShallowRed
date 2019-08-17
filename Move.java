package chess;

import pieces.Piece;

public class Move
{
    private final int ORIGIN;
    private final int DESTIN;
    
    Move(Piece p, int[] move)
    {
        ORIGIN = (p.rank << 3) + p.file;
        DESTIN = (move[0] << 6) + (move[1] << 3) + (move.length > 2 ? move[2] : 0);
    }
    
    public int originRank() { return ORIGIN >> 3; }
    public int originFile() { return ORIGIN & 0b111; }
    
    public int rank() { return DESTIN >> 6; }
    public int file() { return (DESTIN >> 3) & 0b111; }
    public int promotion() { return DESTIN & 0b111;}
    
    public int[] move()
    {
        if(promotion() == 0) {
            return new int[]{rank(), file()};
        }
        return new int[]{rank(), file(), promotion()};
    }
    
    public boolean equals(Move m)
    {
        return (m == null ? false : this.ORIGIN == m.ORIGIN  &&  this.DESTIN == m.DESTIN); 
    }
    public String toString(Piece[][] board)
    {
        Piece MOVER = board[originRank()][originFile()];
        return MOVER.toString() + Utils.getMove(move());
    }
    @Override
    public String toString()
    {
        return Utils.getMove(new int[]{originRank(), originFile()}) + " --> "+ Utils.getMove(move());
    }
}
