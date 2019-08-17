/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package except;

/**
 *
 * @author Mac
 */
public class NullPieceException extends Exception {

    /**
     * Creates a new instance of <code>NullPieceException</code> without detail
     * message.
     */
    public NullPieceException() {
    }

    /**
     * Constructs an instance of <code>NullPieceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NullPieceException(String msg) {
        super(msg);
    }
}
