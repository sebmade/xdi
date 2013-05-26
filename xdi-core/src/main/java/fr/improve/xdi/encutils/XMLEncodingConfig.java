package fr.improve.xdi.encutils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.XIncludeParserConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Handler implementation for reading the enconding config
 * The encoding config is used to limitate the XML export of an object
 * By default a database object is export without his relationships,
 * you must use an enconding config XML file to define the representation 
 * tree of your exported object
 * 
 * The XML grammar oh this file is XMLEncondingConfig.dtd
 * You must declare this in your XML :
 *		&lt;!DOCTYPE config SYSTEM "encodeConfig.dtd"&gt;
 * 
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class XMLEncodingConfig extends DefaultHandler implements Serializable {
	static Log log = LogFactory.getLog(XMLEncodingConfig.class);
	private Hashtable _config = new Hashtable();
	private Stack _stack = new Stack();
	public static final String CONFIG = "config";
	public static final String ENTITY = "entity";
	public static final String KEY = "key";
	public static final String INCLUDES = "includes";
	public static final String EXCLUDES = "excludes";
	public static final String EXTENTS = "extents";
	public static final String ATTRIBUTES = "attributes";
	public static final String TO_ONES = "toOnes";
	public static final String TO_MANYS = "toManys";
	public static final String NAME_ATTR = "name";
	public static final String ALLKEYS_ATTR = "allKeys";
	public static final String FOLLOWRS_ATTR = "followRelationships";
	public static final String ALL_KEYS = "all_keys";
	private boolean _isKey = false;

	public void setConfig(InputSource in_source) throws ParserConfigurationException, SAXException, IOException {
		//log.debug("init parser ...");
		//SAXParserFactory l_factory = SAXParserFactory.newInstance();
        //l_factory.setNamespaceAware(true);
        //l_factory.setXIncludeAware(true);
		//l_factory.setValidating(true);
		//l_factory.setFeature("http://xml.org/sax/features/validation", true);
		//SAXParser l_parser = l_factory.newSAXParser();
		SAXParser l_parser = new SAXParser(new XIncludeParserConfiguration());
		l_parser.setContentHandler(this);
		l_parser.setErrorHandler(this);
		l_parser.setEntityResolver(this);
		//log.debug("parse ...");
		//l_parser.parse(in_source, this); 
        l_parser.parse(in_source);
		//log.debug("ok.");
	}

	public Hashtable getConfig() {
		return _config;
	}

	public Set getKeys(String in_key, String in_type, String in_keyType, String in_rootKey) {
		Hashtable l_rootObj = (Hashtable)_config.get(in_rootKey);
		if (l_rootObj != null) {
			Hashtable l_obj = (Hashtable)l_rootObj.get(in_key);
			if (l_obj != null) {
				return getKeys(l_rootObj, in_key, in_type, in_keyType);
			} else {
				return getKeys(_config, in_key, in_type, in_keyType);
			}
		} else {
			return getKeys(_config, in_key, in_type, in_keyType);
		}
	}

	public Set getKeys(Hashtable in_obj, String in_key, String in_type, String in_keyType) {
		Hashtable l_obj = (Hashtable)in_obj.get(in_key);
		if (l_obj != null) {
			Hashtable l_type = (Hashtable)l_obj.get(in_type);
			if (l_type != null) {
				Set l_keys = (Set)l_type.get(in_keyType);
				if (l_keys != null) {
					return l_keys;
				}
			}
		}
		return new HashSet();
	}

	public boolean followingRelationships(String in_key, String in_rootKey) {
		Hashtable l_rootObj = (Hashtable)_config.get(in_rootKey);
		if (l_rootObj != null) {
			Hashtable l_obj = (Hashtable)l_rootObj.get(in_key);
			if (l_obj != null) {
				return ((Boolean)l_obj.get(FOLLOWRS_ATTR)).booleanValue();
			}
		}
		Hashtable l_obj = (Hashtable)_config.get(in_key);
		if (l_obj != null && l_obj.get(FOLLOWRS_ATTR) != null) {
			return ((Boolean)l_obj.get(FOLLOWRS_ATTR)).booleanValue();
		}
		return false;
	}
    
    @Override
	public void startDocument() throws SAXException {
		_stack.push(_config);
	}

	StringBuilder textContent = new StringBuilder();
	
    @Override
	public void startElement(String in_uri, String in_localName, String in_qName, Attributes in_attributes) throws SAXException {
		String l_name = in_qName;
		_isKey = false;
		if (ENTITY.equals(l_name)) {
			String l_key = in_attributes.getValue(NAME_ATTR);
			Hashtable l_value = new Hashtable();
			l_value.put(FOLLOWRS_ATTR, new Boolean(false));
			String l_followRSStr = in_attributes.getValue(FOLLOWRS_ATTR);
			if (l_followRSStr != null) {
				Boolean l_followRS = Boolean.valueOf(l_followRSStr);
				if (l_followRS.booleanValue()) {
					l_value.put(FOLLOWRS_ATTR, l_followRS);
				}
			} 
			((Hashtable)_stack.peek()).put(l_key, l_value);
			_stack.push(l_value);
		} else if (INCLUDES.equals(l_name) || EXCLUDES.equals(l_name) || EXTENTS.equals(l_name)) {
			Hashtable l_value = new Hashtable();
			((Hashtable)_stack.peek()).put(l_name, l_value);
			_stack.push(l_value);
		} else if (ATTRIBUTES.equals(l_name) || TO_ONES.equals(l_name) || TO_MANYS.equals(l_name)) {
			String l_allKeysStr = in_attributes.getValue(ALLKEYS_ATTR);
			HashSet l_value = new HashSet();
			if (l_allKeysStr != null) {
				Boolean l_allKeys = Boolean.valueOf(l_allKeysStr);
				if (l_allKeys.booleanValue()) {
					l_value.add(ALL_KEYS);
				}
			} 
			((Hashtable)_stack.peek()).put(l_name, l_value);
			_stack.push(l_value);
		} else if (KEY.equals(l_name)) {
			_isKey = true;
		}
		textContent.setLength(0);
	}
    
    @Override
	public void characters(char[] in_ch, int in_start, int in_length) throws SAXException {
        //log.debug("["+new String(in_ch)+"]");
		if (_isKey) {
		    // PATCH : in_length n'est pas tjrs bon et entraine des valeur tronquées
		    // ici on part de in_start et on va chercher le prochain <
//		    if (in_ch[in_start-1] == '>') {
//		        StringBuilder s = new StringBuilder();
//	            for (int i=in_start; i<in_ch.length && in_ch[i]!='<'; i++) {
//	                s.append(in_ch[i]);
//	            }
//		    }
		    textContent.append(new String(in_ch, in_start, in_length));
		}
	}
    
    @Override
	public void endElement(String in_uri, String in_localName, String n_qName) throws SAXException {
	    if (_isKey) {
            ((Set)_stack.peek()).add(textContent.toString());
	    }
		if (!_isKey && !CONFIG.equals(in_localName)) {
			_stack.pop();
		} else {
			_isKey = false;
		}
	}
    
    @Override
	public InputSource resolveEntity(String in_publicId, String in_systemId) throws IOException, SAXException {
	    if (in_systemId.endsWith("encodeConfig.dtd")) {
	        return new InputSource(XMLEncodingConfig.class.getResourceAsStream("XMLEncodingConfig.dtd"));
	    } else {
            return super.resolveEntity(in_publicId, in_systemId);
	    }
	}
    
}
