package fr.improve.xdi;

import com.resurgences.utils.ExceptionUtils;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.improve.xdi.mapping.EnterpriseContext;
import fr.improve.xdi.mapping.EnterpriseObjectCollection;
import fr.improve.xdi.mapping.exception.EnterpriseContextException;
import fr.improve.xdi.resources.Messages;

/**
 * SAX Handler implementation for reading XML to import
 * Implementation of the import process
 *
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class Analyser extends DefaultHandler implements DOMErrorHandler, Serializable {
    protected Log log = LogFactory.getLog(Analyser.class);

    private Document _document = null;

    private Stack _nodeStack = new Stack();

    private EnterpriseObjectCollection _objects = new EnterpriseObjectCollection();

    private transient DocumentBuilder _builder;

    private DOMError _validateError = null;

    private int _lineNumber = 0;

    private XMLDecoder _decoder;

    private EnterpriseContext _enterpriseContext;

    private boolean _retainObjects = false;

    private int _objectCount = 0;

    private String _XSDFileURI = null;

    private int _commitIteration = 1;

    private Text _tmpTxt = null;

    private Node _tmpNode = null;

    private IntegrateErrorHandler _errorHandler = null;

    private BinaryHandler _binaryHandler = null;

    private boolean _willCommit = true;

    private boolean _textFormat = false;

    private Class<? extends IntegrateHandler> _defaultIntegrateHandler;

    public Analyser(XMLDecoder in_decoder) throws ParserConfigurationException {
        DocumentBuilderFactory l_factory = DocumentBuilderFactory.newInstance();
        _builder = l_factory.newDocumentBuilder();
        _document = _builder.newDocument();
        _enterpriseContext = in_decoder.getEnterpriseContext();
        _decoder = in_decoder;
        _nodeStack.push(_document);
    }

    public EnterpriseContext getEnterpriseContext() {
        return _enterpriseContext;
    }

    public void setLogName(String in_name) {
        log = LogFactory.getLog(in_name);
        if (_decoder != null)
            _decoder.setLogName(in_name);
        if (_errorHandler != null)
            _errorHandler.setLogName(in_name);
    }

    public void setLog(Log in_log) {
        log = in_log;
        if (_decoder != null)
            _decoder.setLog(in_log);
        if (_errorHandler != null)
            _errorHandler.setLog(in_log);
    }

    public void setDateFormat(String in_pattern) {
        _decoder.setDateFormat(in_pattern);
    }

    public String getDateFormat() {
        return _decoder.getDateFormat();
    }

    public void setRetainObjects(boolean in_flag) {
        _retainObjects = in_flag;
    }

    public void setXSDFileURI(String in_XSDFileURI) {
        _XSDFileURI = in_XSDFileURI;
    }

    public boolean isRetainObjects() {
        return _retainObjects;
    }

    public void setCommitIteration(int in_value) {
        _commitIteration = in_value;
    }

    public int commitIteration() {
        return _commitIteration;
    }

    public void setErrorHandler(IntegrateErrorHandler in_errorHandler) {
        _errorHandler = in_errorHandler;
        if (_decoder != null) {
            _decoder.setErrorHandler(_errorHandler);
        }
    }

    public void setDefaultIntegrateHandler(Class<? extends IntegrateHandler> in_defaultIntegrateHandler) {
        _defaultIntegrateHandler = in_defaultIntegrateHandler;
        if (_decoder != null) {
            _decoder.setDefaultIntegrateHandler(_defaultIntegrateHandler);
        }
    }
    
    public IntegrateErrorHandler errorHandler() {
        return _errorHandler;
    }

    public void setBinaryHandler(BinaryHandler in_binaryHandler) {
        _binaryHandler = in_binaryHandler;
        if (_decoder != null) {
            _decoder.setBinaryHandler(_binaryHandler);
        }
    }

    public BinaryHandler binaryHandler() {
        return _binaryHandler;
    }

    public void setWillCommit(boolean in_flag) {
        _willCommit = in_flag;
    }

    public Collection getObjects() {
        return _objects;
    }

    public int getObjectCount() {
        return _objectCount;
    }

    public void reset() {
        _objectCount = 0;
        _objects.clear();
        _nodeStack.clear();
        _document = _builder.newDocument();
        _nodeStack.push(_document);
    }

    @Override
    public void startElement(String in_namespaceURI,
                             String in_localName,
                             String in_qName,
                             Attributes in_atts) throws SAXException {
        try {
            if (!in_localName.equals("objects") && !in_localName.equals("export")) {
                //log.debug("startElement : "+in_localName);
                Element l_element = _document.createElementNS(in_namespaceURI, in_qName);
                int nAttrs = in_atts.getLength();

                for (int i = 0; i < nAttrs; i++) {
                    l_element.setAttributeNS(in_atts.getURI(i),
                                             in_atts.getLocalName(i),
                                             in_atts.getValue(i));
                    if (Integrator.FORMAT_KEY.equals(in_atts.getLocalName(i))) {
                        if ("text".equals(in_atts.getValue(i))) {
                            _textFormat = true;
                        }
                    }
                }

                ((Node) _nodeStack.peek()).appendChild(l_element);
                _nodeStack.push(l_element);
            }
        } catch (DOMException e) {
            log.error(e,e);
            log.error("node name : "+((Node) _nodeStack.peek()).getNodeName());
            log.error("node type : "+((Node) _nodeStack.peek()).getNodeType());
            log.error("node text : "+((Node) _nodeStack.peek()).getTextContent());
            log.error("element : "+in_localName+" "+in_qName);
        }
    }

    @Override
    public void endElement(String in_namespaceURI, String in_localName, String in_qName)
            throws SAXException {
        if (!in_localName.equals("objects") && !in_localName.equals("export")) {
            //log.debug("endElement : "+in_localName);
            _textFormat = false;
            _nodeStack.pop();

            if (_nodeStack.peek() == _document) {
                _objectCount++;

                if ((_document.getDocumentElement() != null) && _document.getDocumentElement().hasChildNodes()) {
                    String l_lineNumber = _document.getDocumentElement().getAttribute(Integrator.LINE_KEY);
                    if (l_lineNumber != null && l_lineNumber.length() > 0) {
                        _lineNumber = Integer.valueOf(l_lineNumber).intValue();
                    } else {
                        _lineNumber++;
                    }
                    _validateError = null;

                    Element l_elt = _document.getDocumentElement();
                    /*String l_ns = l_elt.getAttribute("xmlns:res");
                    if (l_ns == null || l_ns.length() == 0) {
                        l_elt.setAttribute("xmlns:res",
                                           "http://www.resurgences.fr/schema/resurgences");
                    }*/

                    if (_XSDFileURI != null) {
                        l_elt.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                                             "xsi:schemaLocation",
                                             in_namespaceURI + " " + _XSDFileURI);

                        DOMConfiguration l_config = _document.getDomConfig();
                        l_config.setParameter("error-handler", this);
                        l_config.setParameter("validate", Boolean.TRUE);
                        _document.normalizeDocument();
                    }

                    if (log.isDebugEnabled()) {
                        try {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                            StringWriter writer = new StringWriter();
                            transformer.transform(new DOMSource(_document), new StreamResult(writer));
                            log.debug(writer.toString());
                            //LSSerializer serializer = ((DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS")).createLSSerializer();
                            //serializer.getDomConfig().setParameter("format-pretty-print", "true");
                            //log.debug(serializer.writeToString(_document));
                        } catch (Exception e) {
                            ExceptionUtils.rethrowIfNeeded(e);
                            log.warn(Messages.getMessage("logError00"), e);
                        }
                    }

                    if (_validateError == null) {
                        Object l_obj = null;
                        boolean l_isCommitted = false;

                        try {
                            log.info(Messages.getMessage("beginDecodeXML01", in_localName));
                            _enterpriseContext.beginTransaction();

                            log.debug(Messages.getMessage("beginDecodeXML00"));
                            l_obj = _decoder.decodeObjectForNode(_document);
                            /*if (l_obj != null) {
                             _enterpriseContext.update((EnterpriseObject)l_obj);
                             }*/
                            log.debug(Messages.getMessage("endDecodeXML00"));

                            if ((l_obj != null) && _retainObjects) {
                                _objects.add(l_obj);
                            }

                            if (_willCommit) {
                                if ((_objectCount % _commitIteration) == 0) {
                                    log.debug(Messages.getMessage("saveChangesInDatabase00"));
                                    _enterpriseContext.saveChanges();
                                    log.debug(Messages.getMessage("saveChangesInDatabase01"));
                                }
                            }
                            l_isCommitted = true;
                            if (l_obj != null) {
                                log.info(Messages.getMessage("objectIntegrated00", l_obj.toString()));
                            }

                        } catch (IntegrateException e) {
                            if (_errorHandler != null) {
                                _errorHandler.integrateError(e, _document, _lineNumber);
                            } else {
                                log.error(Messages.getMessage("integrateObjectFailed00"), e);
                            }
                        } catch (DecodeException e) {
                            if (_errorHandler != null) {
                                _errorHandler.decodeError(e, _document, _lineNumber);
                            } else {
                                log.error(Messages.getMessage("decodeObjectFailed00"), e);
                            }
                        } catch (EnterpriseContextException e) {
                            if (_errorHandler != null) {
                                _errorHandler.saveError(e, _document, _objects, _lineNumber);
                            } else {
                                log.error(Messages.getMessage("commitFailed00"), e);
                            }
                        } catch (Exception e) {
                            ExceptionUtils.rethrowIfNeeded(e);
                            if (_errorHandler != null) {
                                _errorHandler.fatalError(e, _document, _lineNumber);
                            } else {
                                log.fatal(Messages.getMessage("fatalError00"), e);
                            }
                        } finally {
                            log.info(Messages.getMessage("endDecodeXML01", in_localName));
                            try {
                                if (!l_isCommitted) {
                                    _enterpriseContext.invalidateAllObjects();
                                }
                                _enterpriseContext.endTransaction();
                            } catch (EnterpriseContextException e1) {
                                log.debug(Messages.getMessage("rollBackFailed00"), e1);
                            }
                        }
                    } else {
                        if (_errorHandler != null) {
                            _errorHandler.validateError(_validateError, _document, _lineNumber);
                        } else {
                            log.fatal(Messages.getMessage("schemaValidationFailed00"));
                        }
                    }
                }

                _document = _builder.newDocument();
                _nodeStack.pop();
                _nodeStack.push(_document);
            }
        } else {
            if (_willCommit) {
                try {
                    log.debug(Messages.getMessage("saveChangesInDatabase00"));
                    _enterpriseContext.beginTransaction();
                    _enterpriseContext.saveChanges();
                    log.debug(Messages.getMessage("saveChangesInDatabase01"));
                } catch (EnterpriseContextException e) {
                    log.error(Messages.getMessage("commitFailed00"), e);

                    if (_errorHandler != null) {
                        _errorHandler.saveError(e, _document, _objects, _lineNumber);
                    }
                    //try {
                    //    _enterpriseContext.invalidateAllObjects();
                    //} catch (EnterpriseContextException e1) {
                    //    log.error(Messages.getMessage("rollBackFailed00"), e1);
                    //}
                } finally {
                    try {
                        _enterpriseContext.endTransaction();
                    } catch (EnterpriseContextException e1) {
                        log.error(Messages.getMessage("rollBackFailed00"), e1);
                    }
                }
            }
            _lineNumber = 0;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if ((length > 0)) {
            String l_str = new String(ch, start, length);
            if (_textFormat || l_str.trim().length() > 0 || ((ch[start] != '\n') && (ch[start] != '\r'))) {
                //log.debug("characters : "+l_str);
                Node l_node = (Node) _nodeStack.peek();
                if (l_node.getNodeType() != Node.DOCUMENT_NODE) {
                    if (l_node.equals(_tmpNode) && (_tmpTxt != null)) {
                        _tmpTxt.appendData(l_str);
                    } else {
                        _tmpTxt = _document.createTextNode(l_str);
                        _tmpNode = l_node;
                        try {
                            l_node.appendChild(_tmpTxt);
                        } catch (DOMException e) {
                            log.error("???", e);
                        }
                    }
                }
            }
        }
    }

    public boolean handleError(DOMError in_error) {
        _validateError = in_error;

        return false;
    }



}
