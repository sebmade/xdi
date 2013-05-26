package fr.improve.xdi.wo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import fr.improve.xdi.DecodeException;
import fr.improve.xdi.DefaultXMLDecoder;
import fr.improve.xdi.IntegrateException;
import fr.improve.xdi.Integrator;
import fr.improve.xdi.mapping.EnterpriseContext;

public class WOXMLDecoder extends DefaultXMLDecoder {

    public WOXMLDecoder(EnterpriseContext inEnterpriseContext, Class inDefaultIntegrateHandler) {
        super(inEnterpriseContext, inDefaultIntegrateHandler);
    }
    
    public Object decodeObjectForNode(Node in_node) throws DecodeException, IntegrateException {
        switch (in_node.getNodeType()) {
        case Node.ELEMENT_NODE:
            String l_type = ((Element) in_node).getAttribute(Integrator.TYPE_KEY);
            if (l_type.equals(NSDictionary.class.getName())) {
                return _decodeNSDictionary((Element) in_node);
            }

        }
        
        return super.decodeObjectForNode(in_node);
    }

    private NSDictionary _decodeNSDictionary(Element in_element) throws DecodeException, IntegrateException {
        NSMutableDictionary l_dict = new NSMutableDictionary();
        NodeList l_list = in_element.getChildNodes();
        int l_length = l_list.getLength();

        for (int i = 0; i < l_length; i++) {
            Node l_node = l_list.item(i);

            if (l_node.getNodeType() == 1) {
                Object l_obj = decodeObjectForNode(l_node);

                if (l_obj != null) {
                    l_dict.setObjectForKey(l_obj, l_node.getNodeName());
                }
            }
        }

        return l_dict;
    }

}
