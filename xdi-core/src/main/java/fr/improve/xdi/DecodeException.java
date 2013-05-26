package fr.improve.xdi;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class DecodeException extends Exception {

    /**
    * @param message
    */
    public DecodeException(String message) {
        super(message);
    }

    /**
    * @param cause
    */
    public DecodeException(Exception cause) {
        super(cause);
    }

    /**
    * @param message
    * @param cause
    */
    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
