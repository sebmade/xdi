package fr.improve.resedim.converter.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import fr.improve.xdi.Integrator;
import fr.improve.xdi.converter.Converter;
import fr.improve.xdi.converter.ConverterException;


public class CSVConverter implements Converter {

    protected Log log = LogFactory.getLog(CSVConverter.class);
    public static final short XML_TO_CSV_BY_XPATH = 1;
    public static final short XML_TO_CSV_BY_HANDLER = 2;
    private Properties _properties = new Properties();

    /**
    * @see fr.improve.xdi.converter.Converter#convertToXML(java.io.Reader, java.io.Writer)
    */
    public void convertToXML(Reader in_csv, Writer in_writer) throws ConverterException {
        try {
            XMLReader l_xmlReader = new SAXParser(new CSVConfiguration(_properties, null));
            Serializer l_serializer = SerializerFactory.getSerializer(OutputPropertiesFactory.getDefaultMethodProperties(Method.XML));
            l_serializer.setWriter(in_writer);
            l_xmlReader.setContentHandler(l_serializer.asContentHandler());
            l_xmlReader.parse(new InputSource(in_csv));
        } catch (IOException e) {
            throw new ConverterException(e);
        } catch (SAXException e) {
            throw new ConverterException(e);
        }
    }

    /**
    * @see fr.improve.xdi.converter.Converter#convertAndIntegrate(java.io.Reader, java.io.Reader, boolean)
    */
    public void convertAndIntegrate(Integrator in_integrator, Reader in_csv, Reader in_xsl, boolean in_withSchemaValidation) throws ConverterException {
        try {
            TransformerFactory l_factory = TransformerFactory.newInstance();
            Transformer l_transformer = l_factory.newTransformer(new StreamSource(in_xsl));
            if (in_integrator.transformErrorListener() != null) {
                l_transformer.setErrorListener(in_integrator.transformErrorListener());
            }
            XMLReader l_xmlReader = new SAXParser(new CSVConfiguration(_properties, null));
            in_integrator.transformAndIntegrate(in_csv, l_xmlReader, l_transformer);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
    * @see fr.improve.xdi.converter.Converter#convertFromXML(java.io.Reader, java.io.Writer)
    */
    public void convertFromXML(Reader in_reader, Writer in_writer) throws ConverterException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(in_reader), new XMLToCSVHandler(_properties, in_writer));
        } catch (IOException e) {
            throw new ConverterException(e);
        } catch (SAXException e) {
            throw new ConverterException(e);
        } catch (ParserConfigurationException e) {
            throw new ConverterException(e);
        } catch (FactoryConfigurationError e) {
            throw new ConverterException(e);
        }
    }

    /*
     * @see fr.improve.resedim.converter.Converter#convertFromXML(org.w3c.dom.Document, java.io.Writer)
     */
    public void convertFromXML(Document in_document, Writer in_writer) throws ConverterException {
        try {
            // ligne des libellés d'entetes
            int l_numberOfField = Integer.valueOf(_properties.getProperty("number")).intValue();
            for (int l_index = 0; l_index < l_numberOfField; l_index++) {
                String l_name = _properties.getProperty("name"+l_index);
                if (l_name != null) {
                    in_writer.write(_properties.getProperty("name"+l_index));
                }
                in_writer.write(_properties.getProperty("delimiter"));
            }
            in_writer.write('\n');
            // lignes
            NodeList l_list = in_document.getDocumentElement().getChildNodes();
            for (int i=0; i < l_list.getLength(); i++) {
                Node l_node = l_list.item(i);
                for (int l_index = 0; l_index < l_numberOfField; l_index++) {
                    try {
                        NodeList l_subList = XPathAPI.selectNodeList(l_node, _properties.getProperty("field"+l_index));
                        for (int j=0; j<l_subList.getLength(); j++) {
                            Node l_subNode = l_subList.item(j);
                            String l_value = l_subNode.getNodeValue();
                            if (l_value != null && l_value.trim().length()>0) {
                                try {
                                    l_value.replace('\n', ' ');
                                    l_value.replace('\r', ' ');
                                    in_writer.write(l_value);
                                } catch (IOException e) {
                                    log.error("can't write value"+l_value,e);
                                    in_writer.write("#ERROR#");
                                }
                            }
                            in_writer.write(_properties.getProperty("delimiter"));
                        }
                        if (l_subList.getLength() == 0) {
                            in_writer.write(_properties.getProperty("delimiter"));
                        }
                    } catch (DOMException e) {
                        log.error("xpath error : "+_properties.getProperty("field"+l_index),e);
                        in_writer.write("#ERROR#");
                    } catch (TransformerException e) {
                        log.error("xpath error : "+_properties.getProperty("field"+l_index),e);
                        in_writer.write("#ERROR#");
                    }
                }
                in_writer.write('\n');
            }
        } catch (NumberFormatException e) {
            throw new ConverterException(e);
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }

    /**
    * @throws IOException
    * @see fr.improve.xdi.converter.Converter#setConfigFile(java.lang.String)
    */
    public void setConfig(URL in_filePath) throws IOException {
        _properties.clear();
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

}
