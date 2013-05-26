package fr.improve.xdi;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.inject.Inject;

import fr.improve.xdi.converter.Converter;
import fr.improve.xdi.converter.ConverterException;
import fr.improve.xdi.converter.ConverterFactory;
import fr.improve.xdi.mapping.EnterpriseContext;

/**
 * Import controller
 *
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class Integrator implements Serializable {
    protected Log log = LogFactory.getLog(Integrator.class);

    public static final String CREATE_OR_UPDATE = "createOrUpdate";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String MERGE = "merge";
    public static final String SEARCH = "search";
    public static final String REPLACE = "replace";
    public static final String FUSION = "fusion";
    public static final String ACTION_KEY = "action";
    public static final String LINE_KEY = "line";
    public static final String CASCADE_KEY = "cascade";
    public static final String TYPE_KEY = "type";
    //public static final String SEARCH_KEY = "searchKey";
    public static final String SEARCH_XPATH = "child::*[@searchKey='true']";
    public static final String FORMAT_KEY = "format";
    public static final String HANDLER_KEY = "handler";
    public static final String CASE_KEY = "case";
    public static final String TO_ONE_RELATIONSHIP = "toOneRelationship";
    public static final String TO_MANY_RELATIONSHIP = "java.util.HashSet";
    public static final String BINARY = "binary";
    public static final String ENTERPRISE_OBJECT = "enterpriseObject";
    public static final String UPPER_CASE = "upper";
    public static final String LOWER_CASE = "lower";
    public static final String DEFAULT_HANDLER = "fr.improve.xdi.DefaultIntegrateHandler";

    public static String DEFAULT_DECIMAL_FORMAT = "#0.00";
    public static String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";

    //private EnterpriseContext _enterpriseContext;
    private Analyser _analyser;
    private ErrorListener _transformErrorListener = null;

    @Inject
    public Integrator(XMLDecoder in_decoder) throws ParserConfigurationException, InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        //_enterpriseContext = null;
        _analyser = null;
        //_enterpriseContext = in_enterpriseContext;
        _analyser = new Analyser(in_decoder);
    }

    /**
     * Retain or not the imported objects in memory
     * Default : false
     * @param in_flag
     */
    public void setRetainObjects(boolean in_flag) {
        _analyser.setRetainObjects(in_flag);
    }

    public boolean isRetainObjects() {
        return _analyser.isRetainObjects();
    }

    /**
     * Reset the retained objects array
     *
     */
    public void reset() {
        _analyser.reset();
    }

    /**
     * Set the commit iteration of objects
     * @param in_value
     */
    public void setCommitIteration(int in_value) {
        _analyser.setCommitIteration(in_value);
    }

    public int commitIteration() {
        return _analyser.commitIteration();
    }

    /**
     * Set the default date format define in the XML to import
     * @param in_pattern
     */
    public void setDateFormat(String in_pattern) {
        _analyser.setDateFormat(in_pattern);
    }

    public String getDateFormat() {
        return _analyser.getDateFormat();
    }

    /**
     * Set the error handler
     * @param in_errorHandler
     */
    public void setErrorHandler(IntegrateErrorHandler in_errorHandler) {
        _analyser.setErrorHandler(in_errorHandler);
    }

    public void setDefaultIntegrateHandler(Class<? extends IntegrateHandler> in_defaultIntegrateHandler){
        _analyser.setDefaultIntegrateHandler(in_defaultIntegrateHandler);
        
    }
    public IntegrateErrorHandler errorHandler() {
        return _analyser.errorHandler();
    }

    /**
     * Set the transform error listener
     * @param in_transformErrorListener
     */
    public void setTransformErrorListener(ErrorListener in_transformErrorListener) {
        _transformErrorListener = in_transformErrorListener;
    }

    public ErrorListener transformErrorListener() {
        return _transformErrorListener;
    }

    /** Set thr binary handler to import binary data
     *
     * @param in_binaryHandler
     */
    public void setBinaryHandler(BinaryHandler in_binaryHandler) {
        _analyser.setBinaryHandler(in_binaryHandler);
    }

    public BinaryHandler binaryHandler() {
        return _analyser.binaryHandler();
    }

    /**
     * Set the log name, important to separate log from different import process
     * Default : fr.improve.xdi.Integrator
     * @param in_name
     */
    public void setLogName(String in_name) {
        log = LogFactory.getLog(in_name);
        _analyser.setLogName(in_name);
    }

    public void setLog(Log in_log) {
        log = in_log;
        _analyser.setLog(in_log);
    }

    /**
     * Get the retained objects imported
     * @return
     */
    public Collection getObjects() {
        return _analyser.getObjects();
    }

    /**
     * Set the XMLSchema definition of the basic XML format which represent the database
     * Default : no validation is used
     * @param in_uri
     */
    public void setXSDFileURI(String in_uri) {
        _analyser.setXSDFileURI(in_uri);
    }

    /**
     * Get the database context
     * @return
     */
    public EnterpriseContext enterpriseContext() {
        return _analyser.getEnterpriseContext();
    }

    /**
     * Set the commit action, for simulation and test
     * Default : false
     * @param in_flag
     */
    public void setWillCommit(boolean in_flag) {
        _analyser.setWillCommit(in_flag);
    }

    /**
     * Get the number of imported objects
     * @return
     */
    public int getObjectCount() {
        return _analyser.getObjectCount();
    }

    /**
     * XML Transformation and importation of the data
     * @param in_xml : souce XML file
     * @param in_xsl : XSL transformation file
     * @param in_withSchemaValidation : use XMLSchema validation
     * @throws SAXException
     * @throws TransformerException
     */
    public void transformAndIntegrate(Reader in_xml, Reader in_xsl, boolean in_withSchemaValidation)
            throws SAXException, TransformerException {
        if (in_xsl != null) {
            TransformerFactory l_factory = TransformerFactory.newInstance();
            Transformer l_transformer = l_factory.newTransformer(new StreamSource(in_xsl));
            XMLReader l_parser = XMLReaderFactory.createXMLReader();

            if (_transformErrorListener != null) {
                l_transformer.setErrorListener(_transformErrorListener);
            }

            if (in_withSchemaValidation) {
                l_parser.setFeature("http://xml.org/sax/features/validation", true);
                l_parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            }

            l_transformer.transform(new SAXSource(l_parser, new InputSource(in_xml)),
                                    new SAXResult(_analyser));
        } else {
            try {
                integrate(in_xml);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    /**
     * XML Transformation and importation of the data
     * @param in_xml : source XML file
     * @param in_transformer : transformer
     * @param in_withSchemaValidation : use XMLSchema validation
     * @throws SAXException
     * @throws TransformerException
     */
    public void transformAndIntegrate(Reader in_xml,
                                      Transformer in_transformer,
                                      boolean in_withSchemaValidation) throws SAXException,
            TransformerException {
        XMLReader l_parser = XMLReaderFactory.createXMLReader();

        if (in_withSchemaValidation) {
            l_parser.setFeature("http://xml.org/sax/features/validation", true);
            l_parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        }

        in_transformer.transform(new SAXSource(l_parser, new InputSource(in_xml)),
                                 new SAXResult(_analyser));
    }

    /**
     * XML Transformation and importation of the data
     * @param in_xml : source XML file
     * @param in_parser : XML parser
     * @param in_transformer : transformer
     * @throws TransformerException
     */
    public void transformAndIntegrate(Reader in_xml, XMLReader in_parser, Transformer in_transformer)
            throws TransformerException {
        in_transformer.transform(new SAXSource(in_parser, new InputSource(in_xml)),
                                 new SAXResult(_analyser));
    }

    /**
     * Importation of the data
     * @param in_document : source XML document
     * @param in_xsl : XSL transformation file
     * @return : objects imported
     * @throws TransformerException
     */
    public Collection integrate(Node in_document, Reader in_xsl) throws TransformerException {
        Transformer l_transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(in_xsl));
        l_transformer.transform(new DOMSource(in_document), new SAXResult(_analyser));

        return getObjects();
    }

    /**
     * Importation of the data
     * @param in_document : source XML document
     * @param in_transformer : transformer
     * @return
     * @throws TransformerException
     */
    public Collection integrate(Node in_document, Transformer in_transformer)
            throws TransformerException {
        in_transformer.transform(new DOMSource(in_document), new SAXResult(_analyser));

        return getObjects();
    }

    /**
     * Convert, transform and import the data
     * @param in_file : source file
     * @param in_xsl : XSL transformation file
     * @param in_type : conversion type
     * @param in_properties : properties for conversion
     * @param in_withSchemaValidation : use XMLSchema validation
     * @throws ConverterException
     */
    public void convertAndIntegrate(Reader in_file,
                                    Reader in_xsl,
                                    int in_type,
                                    URL in_properties,
                                    boolean in_withSchemaValidation) throws ConverterException {
        try {
            Converter l_converter = ConverterFactory.getInstance().getConverter(in_type);
            l_converter.setConfig(in_properties);
            l_converter.convertAndIntegrate(this, in_file, in_xsl, in_withSchemaValidation);
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }

    /**
     * Importation of the data
     * @param in_xmlReader : source XML file
     * @throws IOException
     * @throws SAXException
     */
    public void integrate(Reader in_xmlReader) throws IOException, SAXException {
        XMLReader l_parser = XMLReaderFactory.createXMLReader();
        l_parser.setContentHandler(_analyser);
        l_parser.parse(new InputSource(in_xmlReader));
    }

    /**
     * Importation of the data
     * @param in_parser : parser
     * @param in_source : source XML file
     * @throws IOException
     * @throws SAXException
     */
    public void integrate(XMLReader in_parser, InputSource in_source) throws IOException, SAXException {
        in_parser.setContentHandler(_analyser);
        in_parser.parse(in_source);
    }

}