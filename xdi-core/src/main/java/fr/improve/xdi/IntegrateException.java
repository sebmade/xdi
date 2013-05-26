package fr.improve.xdi;

/**
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class IntegrateException extends Exception
{

    /**
     * 
     * @param in_message
     */
    public IntegrateException(String in_message)
    {
        super(in_message);
    }

    /**
     * 
     * @param in_cause
     */
    public IntegrateException(Exception in_cause)
    {
        super(in_cause);
    }

    /**
     * 
     * @param in_message
     * @param in_cause
     */
    public IntegrateException(String in_message, Throwable in_cause)
    {
        super(in_message, in_cause);
    }
}
