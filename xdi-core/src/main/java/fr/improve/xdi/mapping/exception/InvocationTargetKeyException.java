package fr.improve.xdi.mapping.exception;

/**
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class InvocationTargetKeyException extends Exception {

    public InvocationTargetKeyException(String message) {
        super(message);
    }

    public InvocationTargetKeyException(Exception cause) {
        super(cause);
    }

    public InvocationTargetKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
