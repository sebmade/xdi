package fr.improve.xdi;

import com.resurgences.utils.ExceptionUtils;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMError;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.improve.xdi.mapping.exception.EnterpriseContextException;
import fr.improve.xdi.resources.Messages;

/**
 * Default implementation of IntegrateErrorHandler
 *
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class DefaultIntegrateErrorHandler implements IntegrateErrorHandler {
    protected Log log = LogFactory.getLog(DefaultIntegrateErrorHandler.class);
    private int _countFileError = 0;
    private int _countSystemError = 0;
    private HashSet _messages = null;

    @Override
    public void setLogName(String in_name) {
        log = LogFactory.getLog(in_name);
    }

    @Override
    public void setLog(Log in_log) {
        log = in_log;
    }

    public Set getMessages() {
        if (_messages == null) {
            _messages = new HashSet();
        }
        return _messages;
    }

    public int countFileRelativeErrors() {
        return _countFileError;
    }

    public int countProcessRelativeErrors() {
        return _countSystemError;
    }

    @Override
    public void validateError(DOMError in_error, Document in_document, int in_lineNumber) throws SAXException {
        _countFileError++;
        log.error(Messages.getMessage("schemaValidationFailed00"));
        getMessages().add(Messages.getMessage("genericErrorMsg00", String.valueOf(in_lineNumber), in_error.getMessage()));
    }

    @Override
    public void fatalError(Throwable in_exception, Document in_document, int in_lineNumber) throws SAXException {
        _countSystemError++;
        log.fatal(Messages.getMessage("fatalError00"), in_exception);
        getMessages().add(Messages.getMessage("genericErrorMsg00", String.valueOf(in_lineNumber), in_exception.getMessage()));
    }

    @Override
    public void saveError(
        EnterpriseContextException in_exception,
        Document in_document,
        Object in_obj, int in_lineNumber) throws SAXException {
        _countSystemError++;
        log.error(Messages.getMessage("commitFailed00"), in_exception);
        getMessages().add(Messages.getMessage("genericErrorMsg00", String.valueOf(in_lineNumber), in_exception.getMessage()));
    }

    @Override
    public void decodeError(DecodeException in_exception, Document in_document, int in_lineNumber)
        throws SAXException {
        _countFileError++;
        log.error(Messages.getMessage("decodeObjectFailed00"), in_exception);
        getMessages().add(Messages.getMessage("genericErrorMsg00", String.valueOf(in_lineNumber), in_exception.getMessage()));
    }

    @Override
    public void integrateError(
        IntegrateException in_exception,
        Document in_document, int in_lineNumber)
        throws SAXException {
        _countFileError++;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter l_writer = new StringWriter();
            transformer.transform(new DOMSource(in_document), new StreamResult(l_writer));
            log.error(l_writer.toString());
        } catch (Exception e1) {
            ExceptionUtils.rethrowIfNeeded(e1);
            log.debug(Messages.getMessage("logError00"));
        }

        log.error(Messages.getMessage("integrateObjectFailed00"), in_exception);
        getMessages().add(Messages.getMessage("genericErrorMsg00", String.valueOf(in_lineNumber), in_exception.getMessage()));
    }

    /**
    * @see fr.improve.xdi.IntegrateErrorHandler#continueAfterDecodeException(fr.improve.xdi.DecodeException)
    */
    @Override
    public boolean continueAfterDecodeException(DecodeException in_exception) {
        /*if (in_exception.getCause() instanceof ObjectAlreadyInRelationException) {
            return true;
        }*/
        return false;
    }

}
