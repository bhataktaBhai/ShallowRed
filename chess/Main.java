package chess;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Hello");
        for(int[] square : Utils.squaresBetween(0, 4, 3, 7))
        {
            System.out.println("Hi");
            System.out.format("%c%d\n", square[1] + 'a', square[0] + 1);
        }
        new Chess().play();
    }
}