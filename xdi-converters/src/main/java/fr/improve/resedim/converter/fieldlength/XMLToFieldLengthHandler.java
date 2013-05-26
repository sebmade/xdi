package fr.improve.resedim.converter.fieldlength;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class XMLToFieldLengthHandler extends DefaultHandler {

    private Properties _properties;

    private String _line;

    private String[] _values;

    private int _index = -1;

    private Writer _writer;

    public XMLToFieldLengthHandler(Properties in_properties, Writer in_writer) {
        _properties = in_properties;
        int l_fields = Integer.valueOf(_properties.getProperty("number")).intValue();
        char l_delim = _properties.getProperty("delimiter").charAt(0);
        StringBuffer l_buffer = new StringBuffer();
        for (int i = 0; i < l_fields; i++) {
            l_buffer.append("{" + i + "}");
            if (i < l_fields - 1) {
                l_buffer.append(l_delim);
            }
        }
        _line = l_buffer.toString();
        _values = new String[l_fields];
        _writer = in_writer;
    }

    /*
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String in_namespaceURI, String in_localName, String in_qName, Attributes in_atts) throws SAXException {
        String l_name = in_localName;
        if (l_name == null || l_name.length() == 0) {
            l_name = in_qName;
        }
        boolean isSetIndex = false;
        if (_properties.containsValue(l_name)) {
            for (Iterator l_keys = _properties.keySet().iterator(); l_keys.hasNext();) {
                String l_key = (String) l_keys.next();
                if (l_key.startsWith("field") && _properties.getProperty(l_key).equals(l_name)) {
                    _index = Integer.valueOf(l_key.substring(l_key.length() - 1)).intValue();
                    isSetIndex = true;
                    break;
                }
            }
        }
        if (!isSetIndex) {
            _index = -1;
        }
    }

    /*
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String in_namespaceURI, String in_localName, String in_qName) throws SAXException {
        try {
            String l_name = in_localName;
            if (l_name == null || l_name.length() == 0) {
                l_name = in_qName;
            }
            for (Iterator l_keys = _properties.keySet().iterator(); l_keys.hasNext();) {
                String l_key = (String) l_keys.next();
                if (l_key.equals("record") && _properties.getProperty(l_key).equals(l_name)) {
                    _writer.write(MessageFormat.format(_line, _values));
                    _writer.write('\n');
                    break;
                }
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /*
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] in_ch, int in_start, int in_length) throws SAXException {
        if (_index != -1) {
            if (_values[_index] != null) {
                _values[_index] += new String(in_ch, in_start, in_length);
            } else {
                _values[_index] = new String(in_ch, in_start, in_length);
            }
        }
    }

}