package it.unitn.repoman.core.exceptions;

@SuppressWarnings("serial")
public class InvalidLineException extends Exception {

    public InvalidLineException(int line) {
        super("ERROR! Invalid line number --> " + line);
    }
}