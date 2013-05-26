package fr.improve.xdi.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.PrefixedQName;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.SerializerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Web service implementation
 * use AXIS 1.1
 * 
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class WebService implements Service {
    static Log log = LogFactory.getLog(WebService.class);

    public Document callService(Hashtable in_parameters)
            throws ServiceException {
        try {
            if (Service.WEB_SERVICE_MSG_TYPE.equals(in_parameters.get(Service.WEB_SERVICE_TYPE))) {
                log.debug("WEB SERVICE MSG TYPE CALL ...");
                org.apache.axis.client.Service l_service = new org.apache.axis.client.Service();
                Call l_call = (Call) l_service.createCall();
                l_call.setTargetEndpointAddress(new URL((String) in_parameters.get(Service.WEB_SERVICE_TARGET_ENDPOINT)));
                log.debug("target engpoint : "+l_call.getTargetEndpointAddress());
				Collection l_attachments = (Collection) in_parameters.get(Service.WEB_SERVICE_ATTACHMENTS);
				if (l_attachments != null && !l_attachments.isEmpty()) {
					Iterator l_list = l_attachments.iterator();
					while (l_list.hasNext()) {
						l_call.addAttachmentPart((AttachmentPart) l_list.next());
					}
					log.debug("Attachments : "+l_attachments.size());
				}

                l_call.setOperationStyle(Style.DOCUMENT);
                l_call.setOperationUse(Use.LITERAL);

                SOAPBodyElement l_bodyElement = new SOAPBodyElement(new PrefixedQName((String) in_parameters.get(Service.WEB_SERVICE_NAMESPACE),
                                                                    (String) in_parameters.get(Service.WEB_SERVICE_NAME),
                                                                    Service.WEB_SERVICE_NSPREFIX));
                Node l_node = (Node)in_parameters.get(Service.WEB_SERVICE_DOCUMENT);
                Element l_element = null;
                switch (l_node.getNodeType()) {
                	case Node.DOCUMENT_NODE : {
                        l_element = ((Document)l_node).getDocumentElement();
                         break;
                	}
                	case Node.ELEMENT_NODE : {
                	    l_element = (Element)l_node;
                        break;
                	}
                }
                l_bodyElement.addChild(new MessageElement(l_element));
                if (log.isDebugEnabled()) {
                    StringWriter l_writer = new StringWriter();
                    SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(l_writer, new OutputFormat(Method.XML, "iso-8859-1", true)).asDOMSerializer().serialize(l_element);
                    log.debug("XML message to send :");
                    log.debug(l_writer.toString());
                }

                Object l_result = l_call.invoke(new Object[] { l_bodyElement });
                RPCElement l_resultRPC = (RPCElement) ((Vector) l_result).elementAt(0);
                
                log.debug("CALL TERMINATED.");
                return XMLUtils.newDocument(new InputSource(new ByteArrayInputStream(l_resultRPC.toString().getBytes())));
            } else {
                // RPC
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return null;
    }
}