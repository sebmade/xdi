package fr.improve.xdi.mapping.eof;

import java.util.Collection;
import java.util.Enumeration;

import org.w3c.dom.Element;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import fr.improve.xdi.DefaultXMLEncoder;

/**
 * XMLEncoder implementation for EOF
 * Used to convert EOF objects to EnterpriseObject
 *
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class XMLEncoderImpl extends DefaultXMLEncoder {

    /**
    *
    */
    public XMLEncoderImpl() {
        super();
    }

    /**
    * @see fr.improve.edi.integrator.XMLEncoder#encodeObjects(java.lang.Object)
    */
    public void encodeObjects(Object in_records) {
        if (in_records instanceof NSArray) {
            super.encodeObjects(new NSArraySet((NSArray)in_records));
        }
    }

    public void encodeObjects(Object in_records, String in_rootName) {
        if (in_records instanceof NSArray) {
            super.encodeObjects(new NSArraySet((NSArray)in_records), in_rootName);
        } else if (in_records instanceof NSDictionary)
            encodeObjects((NSDictionary) in_records, in_rootName);
    }

    public void encodeObjects(NSDictionary in_records, String in_rootName) {
        log.debug("encode NSDictionary ...");
        _elementName = in_rootName;
        encodeObjectForKey(in_records, _elementName, null);
        log.debug("encode ok.");
    }

    private void _encodeNSDictionaryForKey(NSDictionary in_obj, String in_key, Element in_parent) {
        Element l_elt;

        if (_document == null || in_parent == null) {
            _document = _domImpl.createDocument(null, in_key, null);
            l_elt = _document.getDocumentElement();
            in_parent = l_elt;
            _rootEntityElement = l_elt;
        } else {
            l_elt = _document.createElement(in_key);
            in_parent.appendChild(l_elt);
            if (in_parent.equals(_document.getDocumentElement())) {
                _rootEntityElement = l_elt;
            }
        }

        Enumeration l_keys = in_obj.keyEnumerator();
        while (l_keys.hasMoreElements()) {
            String l_key = (String) l_keys.nextElement();
            Object l_obj = in_obj.get(l_key);

            if (l_obj instanceof EOEnterpriseObject) {
                l_obj = new EnterpriseObjectImpl((EOEnterpriseObject) l_obj);
                Element l_eltOneR = _document.createElement(l_key);
                encodeObjectForKey(l_obj, l_key, l_eltOneR);
                l_elt.appendChild(l_eltOneR);
            } else
                encodeObjectForKey(l_obj, l_key, l_elt);
        }
    }

    public void encodeObjectForKey(Object in_obj, String in_key, Element in_parent) {
        if (in_obj != null) {
            if (in_obj instanceof NSDictionary) {
                _encodeNSDictionaryForKey((NSDictionary) in_obj, in_key, in_parent);

                return;
            }
        }
        super.encodeObjectForKey(in_obj, in_key, in_parent);
    }

    public void encodeObjects(Collection in_records, String in_rootName) {
        if (in_records instanceof NSArray) {
            super.encodeObjects(new NSArraySet((NSArray)in_records), in_rootName);
        } else
            super.encodeObjects(in_records, in_rootName);
    }

    /**
    * @see fr.improve.edi.integrator.XMLEncoder#encodeObject(java.lang.Object)
    */
    public void encodeObject(Object in_record) {
        if (in_record instanceof EOEnterpriseObject) {
            super.encodeObject(new EnterpriseObjectImpl((EOEnterpriseObject)in_record));
        }
    }

}
