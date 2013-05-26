package fr.improve.xdi;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.w3c.dom.DOMError;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.improve.xdi.mapping.exception.EnterpriseContextException;

/**
 * Interface to implement for handling integration process error
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface IntegrateErrorHandler extends Serializable {

	/**
	 * @param in_name
	 */
   public void setLogName(String in_name);

    /**
     * @param in_log
     */
   public void setLog(Log in_log);
   
	/**
	 * @param in_error
	 * @param in_document
	 */
	public void validateError(DOMError in_error, Document in_document, int in_lineNumber) throws SAXException;

	/**
	 * @param in_exception
	 * @param in_document
	 */
	public void fatalError(Throwable in_exception, Document in_document, int in_lineNumber) throws SAXException;

	/**
	 * @param in_exception
	 * @param in_document
	 * @param l_obj
	 */
	public void saveError(EnterpriseContextException in_exception, Document in_document, Object in_obj, int in_lineNumber) throws SAXException;

	/**
	 * @param in_exception
	 * @param in_document
	 */
	public boolean continueAfterDecodeException(DecodeException in_exception);

	/**
	 * @param in_exception
	 * @param in_document
	 */
	public void decodeError(DecodeException in_exception, Document in_document, int in_lineNumber) throws SAXException;

	/**
	 * @param in_exception
	 * @param in_document
	 */
	public void integrateError(IntegrateException in_exception, Document in_document, int in_lineNumber) throws SAXException;

}
