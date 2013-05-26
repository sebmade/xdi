package fr.improve.xdi.service;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class ServiceException extends Exception {

    /**
     * @param message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ServiceException(Exception cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
