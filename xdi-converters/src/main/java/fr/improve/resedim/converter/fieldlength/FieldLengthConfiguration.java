package fr.improve.resedim.converter.fieldlength;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.QName;

import fr.improve.resedim.converter.AbstractXMLConfiguration;

/**
 * Configuration pour parser des fichiers avec des champs de longueur fixes
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 */
public class FieldLengthConfiguration extends AbstractXMLConfiguration {
    static Log log = LogFactory.getLog(FieldLengthConfiguration.class);

    public static final String IGNORED = "#IGNORED#";

    public FieldLengthConfiguration(Properties in_properties, String in_xsdURI) throws IOException {
        super(in_properties, in_xsdURI);
    }

    /**
     * @see fr.improve.resedim.converter.AbstractXMLConfiguration#parse(java.io.BufferedReader)
     */
    public void parse(BufferedReader in_bufferedreader) throws IOException {
        // start document
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(new SimpleLocator(), "iso-8859-1", new NamespaceSupport(), null);
            fDocumentHandler.xmlDecl("1.0", "iso-8859-1", "true", null);
        }

        QName l_rootName = new QName(null, params.getProperty("root"), params.getProperty("root"), null);

        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(l_rootName, xsAttributes(), null);
        }

        // read lines
        String l_line;
        QName l_recordName = new QName(null, params.getProperty("record"), params.getProperty("record"), null);

        while ((l_line = in_bufferedreader.readLine()) != null) {
            if (fDocumentHandler != null && l_line.length() > 0) {
                fDocumentHandler.ignorableWhitespace(NEWLINE_ONE_SPACE, null);
                fDocumentHandler.startElement(l_recordName, EMPTY_ATTRS, null);

                //log.debug("<" + params.getProperty("record") + ">");
                int l_index = 0;

                for (int i = 0; i < fieldNumber; i++) {
                    String l_fieldNameStr = params.getProperty("field" + i);
                    try {
                        int l_fieldLength = Integer.valueOf(params.getProperty("length" + i)).intValue();
                        if (!IGNORED.equals(l_fieldNameStr)) {
                            String l_value;

                            try {
                                l_value = l_line.substring(l_index, l_index + l_fieldLength).trim();
                            } catch (StringIndexOutOfBoundsException e) {
                                if (l_index < l_line.length()) {
                                    l_value = l_line.substring(l_index).trim();
                                } else

                                    break;
                            }

                            if (l_value.length() > 0) {
                                QName l_fieldName = new QName(null, l_fieldNameStr, l_fieldNameStr, null);
                                fDocumentHandler.ignorableWhitespace(NEWLINE_TWO_SPACES, null);
                                fDocumentHandler.startElement(l_fieldName, EMPTY_ATTRS, null);
                                //log.debug("<" + l_fieldNameStr + ">");
                                fStringBuffer.clear();
                                fStringBuffer.append(l_value);
                                //log.debug(l_value);
                                fDocumentHandler.characters(fStringBuffer, null);
                                fDocumentHandler.endElement(l_fieldName, null);
                                //log.debug("</" + l_fieldNameStr + ">");
                            }
                        }

                        l_index += l_fieldLength;
                    } catch (NumberFormatException e) {
                        log.error("Property length" + i + " error",e);
                    }
                }

                fDocumentHandler.ignorableWhitespace(NEWLINE_ONE_SPACE, null);
                fDocumentHandler.endElement(l_recordName, null);

                //log.debug("</" + params.getProperty("record") + ">");
            }
        }

        // end document
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(NEWLINE, null);
            fDocumentHandler.endElement(l_rootName, null);
            fDocumentHandler.endDocument(null);
        }
    }
}