package fr.improve.xdi.mapping.exception;

/**
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class UnknownKeyException extends Exception {

    public UnknownKeyException(String message) {
        super(message);
    }

    public UnknownKeyException(Exception cause) {
        super(cause);
    }

    public UnknownKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
