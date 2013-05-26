package fr.improve.resedim.converter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.xerces.parsers.IntegratedParserConfiguration;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;


/**
 * Abstract class extends Xerces IntegratedParserConfiguration, 
 * to simulate XML parse with another format
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public abstract class AbstractXMLConfiguration extends IntegratedParserConfiguration {
	protected static final XMLAttributes EMPTY_ATTRS = new XMLAttributesImpl();
	protected final XMLString NEWLINE = new XMLStringBuffer("\n");
	protected final XMLString NEWLINE_ONE_SPACE = new XMLStringBuffer("\n ");
	protected final XMLString NEWLINE_TWO_SPACES = new XMLStringBuffer("\n  ");
	protected final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
	protected Properties params;
	protected String xsdURI;
	protected int fieldNumber = -1;

    public AbstractXMLConfiguration(Properties in_properties, String in_xsdURI) {
        params = in_properties;
        xsdURI = in_xsdURI;
        String l_number = params.getProperty("number");
        if (l_number != null) {
            fieldNumber = Integer.valueOf(l_number).intValue();
        }
    }

	public AbstractXMLConfiguration(InputStream in_parameterFile, String in_xsdURI) throws IOException {
		params = new Properties();
		params.load(in_parameterFile);
		xsdURI = in_xsdURI;
        String l_number = params.getProperty("number");
        if (l_number != null) {
            fieldNumber = Integer.valueOf(l_number).intValue();
        }
	}
	
	public Properties getParameters() {
		return params;
	}

    public XMLAttributes xsAttributes() {
        XMLAttributes l_xs = new XMLAttributesImpl();

        if (xsdURI != null) {
            l_xs.addAttribute(new QName(null, null, "xmlns:xsi", null), "CDATA", "http://www.w3.org/2001/XMLSchema-instance");
            l_xs.addAttribute(
                new QName(
                    "xsi",
                    "noNamespaceSchemaLocation",
                    "xsi:noNamespaceSchemaLocation",
                    "http://www.w3.org/2001/XMLSchema-instance"),
                "ID",
                xsdURI);
        }

        return l_xs;
    }

    protected void openInputSourceStream(XMLInputSource in_source)
        throws IOException {
        if (in_source.getCharacterStream() != null) {
            return;
        }

        java.io.InputStream l_stream = in_source.getByteStream();

        if (l_stream == null) {
            String l_systemId = in_source.getSystemId();

            try {
                URL l_url = new URL(l_systemId);
                l_stream = l_url.openStream();
            } catch (MalformedURLException e) {
                l_stream = new FileInputStream(l_systemId);
            }

            in_source.setByteStream(l_stream);
        }
    }

    public void parse(XMLInputSource in_source) throws IOException, XNIException {
        openInputSourceStream(in_source);

        java.io.Reader l_reader = in_source.getCharacterStream();

        if (l_reader == null) {
            java.io.InputStream l_stream = in_source.getByteStream();
            l_reader = new InputStreamReader(l_stream);
        }

        BufferedReader l_bufferedReader = new BufferedReader(l_reader);
        parse(l_bufferedReader);
        l_bufferedReader.close();
    }

    public abstract void parse(BufferedReader bufferedreader)
        throws IOException;

}
