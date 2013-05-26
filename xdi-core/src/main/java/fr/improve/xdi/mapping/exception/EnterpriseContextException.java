package fr.improve.xdi.mapping.exception;

/**
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EnterpriseContextException extends Exception {

    public EnterpriseContextException(String message) {
        super(message);
    }

    public EnterpriseContextException(Exception cause) {
        super(cause);
    }

    public EnterpriseContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
