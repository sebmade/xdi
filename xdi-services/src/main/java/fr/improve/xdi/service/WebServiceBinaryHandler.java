package fr.improve.xdi.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.attachments.Attachments;
import org.apache.axis.attachments.ManagedMemoryDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.improve.xdi.BinaryHandler;
import fr.improve.xdi.resources.Messages;

/**
 * BinaryHandler implementation for web service 
 * Attach binary data with XML using MIME (SOAP With Attachment)
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *  
 */
public class WebServiceBinaryHandler implements BinaryHandler {
    static Log log = LogFactory.getLog(WebServiceBinaryHandler.class);

    private Set _attachmentsToSend = null;

    private Attachments _attachmentsReceived = null;

    private String _contentType = null;

    public WebServiceBinaryHandler(Attachments in_attachments) {
        _attachmentsReceived = in_attachments;
    }

    public WebServiceBinaryHandler() {
        _attachmentsToSend = new HashSet();
    }

    public Object decode(String in_contentID) {
        try {
            AttachmentPart l_attachmentPart = (AttachmentPart) _attachmentsReceived.getAttachmentByReference(in_contentID);
            if (l_attachmentPart != null) {
                InputStream l_stream = l_attachmentPart.getDataHandler().getInputStream();
                ByteArrayOutputStream l_writer = new ByteArrayOutputStream();
                int l_byte;
                while ((l_byte = l_stream.read()) != -1) {
                    l_writer.write(l_byte);
                }
                return l_writer.toByteArray();
            } else {
                log.error(Messages.getMessage("attachmentDecode00", in_contentID));
            }
        } catch (AxisFault e) {
            log.error("Erreur au décodage",e);
        } catch (IOException e) {
            log.error("Erreur au décodage",e);
        } catch (SOAPException e) {
            log.error("Erreur au décodage",e);
        }
        return null;
    }

    public String encode(byte[] in_object) {
        try {
            ByteArrayInputStream l_bais = new java.io.ByteArrayInputStream(in_object);
            DataHandler l_datahandler = new DataHandler(new ManagedMemoryDataSource(l_bais,
                                                                                    ManagedMemoryDataSource.MAX_MEMORY_DISK_CACHED,
                                                                                    _contentType,
                                                                                    true));
            AttachmentPart l_attachmentPart = new AttachmentPart(l_datahandler);
            _attachmentsToSend.add(l_attachmentPart);
            return l_attachmentPart.getContentIdRef();
        } catch (IOException e) {
            log.warn("Encoding error",e);
        }
        return null;
    }

    public Collection getAttachments() {
        return _attachmentsToSend;
    }

    /** 
     * @see fr.improve.xdi.BinaryHandler#getContentType()
     */
    public String getContentType() {
        return _contentType;
    }

    /**
     * @see fr.improve.xdi.BinaryHandler#setContentType(java.lang.String)
     */
    public void setContentType(String in_contentType) {
        _contentType = in_contentType;

    }

}