package fr.improve.xdi.service;

import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * HTTP POST XML service
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class HTTPXMLService extends HTTPSenderService {
	static Log log = LogFactory.getLog(HTTPXMLService.class);

    public Document callService(Hashtable in_parameters)
        throws ServiceException {
        try {
            Document l_message = (Document)in_parameters.get(Service.HTTP_XML_DOCUMENT);
            Element l_root = l_message.getDocumentElement();
            Hashtable l_attrs = (Hashtable)in_parameters.get(Service.HTTP_XML_ATTRIBUTES);
            if (l_attrs != null && l_attrs.size() > 0) {
                Iterator l_list = l_attrs.keySet().iterator();
                while (l_list.hasNext()) {
                    String l_key = (String)l_list.next();
                    l_root.setAttribute(l_key, (String)l_attrs.get(l_key));
                }
            }

            OutputFormat l_format = new OutputFormat(l_message, "iso-8859-1", true);
            StringWriter l_writer = new StringWriter();
            XMLSerializer l_serial = new XMLSerializer(l_writer, l_format);
            l_serial.asDOMSerializer();
            l_serial.serialize(l_message);
            in_parameters.put(Service.HTTP_CONTENT, l_writer.toString().getBytes());
            l_writer.close();
            in_parameters.put(Service.HTTP_CONTENT_TYPE, "text/xml");

            return super.callService(in_parameters);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }
    
}
