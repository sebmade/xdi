package fr.improve.xdi;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface to implement for handling binary data import/export
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface BinaryHandler extends Serializable {

	/**
	 * Decode binary data
	 * used in import process
	 * 
	 * @param in_content
	 * @return binary data object
	 */
	public Object decode(String in_content);

	/**
	 * Encode binary data
	 * used in export process
	 * 
	 * @param in_object
	 * @return reference to the object (ContentID)
	 */
	public String encode(byte[] in_object);

	/**
	 * Get all the binary data
	 * 
	 * @return
	 */
	public Collection getAttachments();

    /**
     * Get content type of the binary data (MIME)
     * used in import process
     * 
     * @return content type
     */
    public String getContentType();

    /**
     * Set content type of the binary data (MIME)
     * used in export process
     * 
     * @param in_contentType
     */
    public void setContentType(String in_contentType);

}
