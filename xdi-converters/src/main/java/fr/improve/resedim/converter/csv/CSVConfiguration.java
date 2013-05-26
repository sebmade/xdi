package fr.improve.resedim.converter.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.xni.QName;

import fr.improve.resedim.converter.AbstractXMLConfiguration;


public class CSVConfiguration extends AbstractXMLConfiguration {
    static Log log = LogFactory.getLog(CSVConfiguration.class);
    protected static final QName CSV = new QName(null, null, "csv", null);
    protected static final QName ROW = new QName(null, null, "row", null);
    protected static final QName COL = new QName(null, null, "col", null);
    private char _delimiter;

    public CSVConfiguration(Properties in_properties, String in_xsdURI) throws IOException {
        super(in_properties, in_xsdURI);
        String l_delim = params.getProperty("delimiter");
        if (l_delim != null) {
            _delimiter = l_delim.charAt(0);
        } else {
            _delimiter = ',';
        }
    }

	/**
	 * @see fr.improve.resedim.converter.AbstractXMLConfiguration#parse(java.io.BufferedReader)
	 */
	public void parse(BufferedReader in_bufferedreader) throws IOException {
		if (fDocumentHandler != null) {
			fDocumentHandler.startDocument(new SimpleLocator(), "iso-8859-1", new NamespaceSupport(), null);
			fDocumentHandler.xmlDecl("1.0", "iso-8859-1", "true", null);
		}

        String l_root = params.getProperty("root");
        QName l_rootName = new QName(null, l_root, l_root, null);

		if (fDocumentHandler != null) {
			fDocumentHandler.startElement(l_rootName, xsAttributes(), null);
		}

        String l_record = params.getProperty("record");
        QName l_recordName = new QName(null, l_record, l_record, null);
        int l_attrId = 0;
        String l_attr;
        XMLAttributesImpl l_recordAttrs = new XMLAttributesImpl();
        while ((l_attr = params.getProperty("recordAttribute" + l_attrId)) != null) {
            String l_attrValue = params.getProperty("recordAttributeValue" + (l_attrId++));
            if (l_attrValue != null) {
                l_recordAttrs.addAttribute(new QName(null, l_attr, l_attr, null),
                                           "CDATA",
                                           l_attrValue);
            }
        }

        String line;
		while ((line = in_bufferedreader.readLine()) != null) {
			if ((fDocumentHandler != null) && (line.length() > 0) && (line.indexOf(_delimiter) != -1)) {
				fDocumentHandler.ignorableWhitespace(NEWLINE_ONE_SPACE, null);
				fDocumentHandler.startElement(l_recordName, l_recordAttrs, null);

				StringBuffer l_value = new StringBuffer();
				int id = 0;

				for (int i = 0; i < line.length(); i++) {
					char l_char = line.charAt(i);

					if (l_char != _delimiter) {
						l_value.append(l_char);

						if (i != (line.length() - 1)) {
							continue;
						}
					}

					String l_str = l_value.toString().trim();

                    if (l_str.length() > 0) {
                        String l_field = params.getProperty("field" + id);
                        if (l_field != null) {
                            QName l_fieldName = new QName(null, l_field, l_field, null);
                            fDocumentHandler.ignorableWhitespace(NEWLINE_TWO_SPACES, null);
                            l_attrId = 0;
                            XMLAttributesImpl l_attrs = new XMLAttributesImpl();
                            while ((l_attr = params.getProperty("attribute" + id + l_attrId)) != null) {
                                String l_attrValue = params.getProperty("attributeValue" + id + (l_attrId++));
                                if (l_attrValue != null) {
                                    l_attrs.addAttribute(new QName(null, l_attr, l_attr, null),
                                                         "CDATA",
                                                         l_attrValue);
                                }
                            }
                            fDocumentHandler.startElement(l_fieldName, l_attrs, null);
                            fStringBuffer.clear();

                            String l_numberFormat = params.getProperty("numberFormat" + id);
                            if (l_numberFormat != null && (l_str.length() > 0)) {
                                try {
                                    l_str = (new DecimalFormat(l_numberFormat)).format(Double.valueOf(l_str).doubleValue());
                                } catch (NumberFormatException e) {
                                    log.warn("impossible de formatter la valeur : " + l_str);
                                    l_str = "";
                                }
                            }

                            fStringBuffer.append(l_str);
                            fDocumentHandler.characters(fStringBuffer, null);
                            fDocumentHandler.endElement(l_fieldName, null);
                        }
                    }

					id++;
					l_value = new StringBuffer();
				}

				fDocumentHandler.ignorableWhitespace(NEWLINE_ONE_SPACE, null);
				fDocumentHandler.endElement(l_recordName, null);
			}
		}

		log.debug(l_recordName);

		if (fDocumentHandler != null) {
			fDocumentHandler.ignorableWhitespace(NEWLINE, null);
			fDocumentHandler.endElement(l_rootName, null);
			fDocumentHandler.endDocument(null);
			log.debug(l_rootName);
		}
	}
}
