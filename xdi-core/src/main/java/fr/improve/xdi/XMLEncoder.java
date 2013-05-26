package fr.improve.xdi;

import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.improve.xdi.encutils.XMLEncodingConfig;
import fr.improve.xdi.mapping.Converter;

/**
 * Interface to implement for encode objects in XML
 * 
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public interface XMLEncoder extends Serializable {
	public void setConverter(Converter in_converter);
	public Converter getConverter();
	public void setEncodingConfig(InputSource in_source) throws ParserConfigurationException, SAXException, IOException;
	public XMLEncodingConfig getEncodingConfig();
	public void setFollowingRelationships(boolean in_flag);
	public boolean isFollowingRelationships();
	public Document getDocument();
	public void serializeToFile(String in_filePath);
    public void serializeToFile(String in_filePath, String in_encoding);
	public String serialize() throws IOException;
	public void encodeObjects(Object in_records);
	public void encodeObjects(Object in_records, String in_rootName);
	public void encodeObject(Object in_record);
	public void setBinaryHandler(BinaryHandler in_binaryHandler);
	public BinaryHandler binaryHandler();
	public String getContentType(Element in_element);
	public void setDateFormat(String in_pattern);
    public void setDisableEmptyString(boolean in_flag);
}
