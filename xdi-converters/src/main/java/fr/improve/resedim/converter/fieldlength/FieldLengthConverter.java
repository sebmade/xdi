package fr.improve.resedim.converter.fieldlength;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.improve.xdi.Analyser;
import fr.improve.xdi.Integrator;
import fr.improve.xdi.converter.Converter;
import fr.improve.xdi.converter.ConverterException;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class FieldLengthConverter implements Converter {
    private Properties _properties = new Properties();

    protected Log log = LogFactory.getLog(Analyser.class);

    /**
    * @see fr.improve.xdi.converter.Converter#setConfigFile(java.net.URL)
    */
    public void setConfig(URL in_filePath) throws IOException {
        _properties.load(in_filePath.openStream());
    }
    public void setConfig(Properties in_properties) throws IOException {
        _properties = in_properties;
    }

    /*
     * @see fr.improve.resedim.converter.Converter#getConfig()
     */
    public Properties getConfig() {
        return _properties;
    }

    /**
    * @see fr.improve.xdi.converter.Converter#convertAndIntegrate(java.io.Reader, java.io.Reader, boolean)
    */
    public void convertAndIntegrate(Integrator in_integrator, Reader in_file, Reader in_xsl, boolean in_withSchemaValidation) throws ConverterException {
        try {
            TransformerFactory l_factory = TransformerFactory.newInstance();
            Transformer l_transformer = l_factory.newTransformer(new StreamSource(in_xsl));
            if (in_integrator.transformErrorListener() != null) {
                l_transformer.setErrorListener(in_integrator.transformErrorListener());
            }
            XMLReader l_xmlReader = new SAXParser(new FieldLengthConfiguration(_properties, null));
            in_integrator.transformAndIntegrate(in_file, l_xmlReader, l_transformer);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
    * @see fr.improve.xdi.converter.Converter#convertToXML(java.io.Reader, java.io.Writer)
    */
    public void convertToXML(Reader in_reader, Writer in_writer) throws ConverterException {
        try {
            XMLReader l_xmlReader = new SAXParser(new FieldLengthConfiguration(_properties, null));
            Serializer l_serializer = SerializerFactory.getSerializer(OutputPropertiesFactory.getDefaultMethodProperties(Method.XML));
            l_serializer.setWriter(in_writer);
            l_xmlReader.setContentHandler(l_serializer.asContentHandler());
            l_xmlReader.parse(new InputSource(in_reader));
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
    * @see fr.improve.xdi.converter.Converter#convertFromXML(java.io.Reader, java.io.Writer)
    */
    public void convertFromXML(Reader in_reader, Writer in_writer) throws ConverterException {
        try {
            TransformerFactory l_factory = TransformerFactory.newInstance();
            Transformer l_transformer = l_factory.newTransformer(new StreamSource(getClass().getResourceAsStream("csv.xsl")));
            l_transformer.transform(new StreamSource(in_reader), new StreamResult(in_writer));
        } catch (TransformerConfigurationException e) {
            throw new ConverterException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new ConverterException(e);
        } catch (TransformerException e) {
            throw new ConverterException(e);
        }
    }

    /*
     * @see fr.improve.resedim.converter.Converter#convertFromXML(org.w3c.dom.Document, java.io.Writer)
     */
    public void convertFromXML(Document in_document, Writer in_writer) throws ConverterException {
        // TODO Auto-generated method stub

    }

}
