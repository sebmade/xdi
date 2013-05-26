package fr.improve.xdi;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EncodeException extends Exception {


	/**
	 * @param message
	 */
	public EncodeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EncodeException(Exception cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EncodeException(String message, Throwable cause) {
		super(message, cause);
	}

}
