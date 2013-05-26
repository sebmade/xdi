package fr.improve.xdi;

import com.resurgences.utils.ExceptionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.improve.xdi.encutils.XMLEncodingConfig;
import fr.improve.xdi.mapping.Converter;
import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.exception.InvocationTargetKeyException;
import fr.improve.xdi.mapping.exception.UnknownKeyException;
import fr.improve.xdi.resources.Messages;

public abstract class DefaultXMLEncoder implements XMLEncoder {
    protected static Log log = LogFactory.getLog(DefaultXMLEncoder.class);

    protected static DocumentBuilderFactory _dFactory;

    protected static DocumentBuilder _dBuilder;

    protected static DOMImplementation _domImpl;

    private static final int ATTRIBUTE = 0;

    private static final int TO_ONE = 1;

    private static final int TO_MANY = 2;

    protected Document _document;

    protected BinaryHandler binaryHandler = null;

    protected boolean useCache = false;

    private Hashtable _encodedObjects;

    protected String _elementName;

    private SimpleDateFormat _format;

    //private Properties _limits;
    private Converter _typeConverter;

    private boolean _followingRelationships;

    private XMLEncodingConfig _encodingConfig;

    private boolean _disableEmptyString = true;

    static {
        try {
            _dFactory = DocumentBuilderFactory.newInstance();
            _dBuilder = _dFactory.newDocumentBuilder();
            _domImpl = _dBuilder.getDOMImplementation();
        } catch (Exception e) {
            ExceptionUtils.rethrowIfNeeded(e);
            log.fatal("Init error", e);
        }
    }

    public DefaultXMLEncoder() {
        _encodedObjects = new Hashtable();
        _elementName = "root";
        _format = new SimpleDateFormat(Integrator.DEFAULT_DATE_FORMAT);
        //_limits = new Properties();
        _typeConverter = null;
        _followingRelationships = false;
        _encodingConfig = null;
    }

    @Override
    public void setDateFormat(String in_pattern) {
        _format = new SimpleDateFormat(in_pattern);
    }

    @Override
    public void setDisableEmptyString(boolean in_flag) {
        _disableEmptyString = in_flag;
    }

    @Override
    public void setBinaryHandler(BinaryHandler in_binaryHandler) {
        binaryHandler = in_binaryHandler;
    }

    @Override   
    public BinaryHandler binaryHandler() {
        return binaryHandler;
    }

    @Override
    public void setConverter(Converter in_converter) {
        _typeConverter = in_converter;
    }

    @Override
    public Converter getConverter() {
        return _typeConverter;
    }

    @Override
    public void setEncodingConfig(InputSource in_source) throws ParserConfigurationException,
            SAXException, IOException {
        _encodingConfig = new XMLEncodingConfig();
        _encodingConfig.setConfig(in_source);
        _followingRelationships = true;
    }

    public void setEncodingConfig(XMLEncodingConfig in_config) {
        _encodingConfig = in_config;
        _followingRelationships = true;
    }

    @Override
    public XMLEncodingConfig getEncodingConfig() {
        return _encodingConfig;
    }

    @Override
    public void setFollowingRelationships(boolean in_flag) {
        _followingRelationships = in_flag;
    }

    @Override
    public boolean isFollowingRelationships() {
        return _followingRelationships;
    }

    @Override
    public Document getDocument() {
        return _document;
    }

    @Override
    public void serializeToFile(String in_filePath) {
        serializeToFile(in_filePath, "UTF-8");
    }

    @Override
    public void serializeToFile(String in_filePath, String encoding) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(_document), new StreamResult(new FileWriter(in_filePath)));
            //LSSerializer serializer = ((DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS")).createLSSerializer();
            //serializer.getDomConfig().setParameter("format-pretty-print", "true");
            //FileUtils.writeStringToFile(new File(in_filePath), serializer.writeToString(_document), encoding);
        } catch (IOException e) {
            log.error(Messages.getMessage("serializeToFile00", new String[] { in_filePath, encoding }), e);
        } catch (TransformerException e) {
            log.error(Messages.getMessage("serializeToFile00", new String[] { in_filePath, encoding }), e);
        }
    }

    @Override
    public String serialize() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(_document), new StreamResult(writer));
            return writer.toString();
            //LSSerializer serializer = ((DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS")).createLSSerializer();
            //serializer.getDomConfig().setParameter("format-pretty-print", "true");
            //return serializer.writeToString(_document);
        } catch (TransformerException e) {
            log.error("Unexpected error", e);
        }
        return null;
    }

    public void encodeObjects(Collection in_records) {
        _elementName = "objects";
        _document = _domImpl.createDocument(null, _elementName, null);
        encodeObjectForKey(in_records, null, _document.getDocumentElement());
    }

    public void encodeObjects(Collection in_records, String in_rootName) {
        _elementName = in_rootName;
        _document = _domImpl.createDocument(null, _elementName, null);
        encodeObjectForKey(in_records, null, _document.getDocumentElement());
    }

    public void encodeObject(EnterpriseObject in_record) {
        encodeObjectForKey(in_record, null, null);
    }

    public void encodeObjectForKey(Object in_obj, String in_key, Element in_parent) {
        if (in_obj != null) {
            if (in_obj instanceof String) {
                _encodeStringForKey((String) in_obj, in_key, in_parent);

                return;
            }

            if (in_obj instanceof Collection) {
                _encodeCollectionForKey((Collection) in_obj, in_key, in_parent);

                return;
            }

            if (in_obj instanceof Hashtable) {
                _encodeDictionaryForKey((Hashtable) in_obj, in_key, in_parent);

                return;
            }

            if (in_obj instanceof Date) {
                _encodeDateForKey((Date) in_obj, in_key, in_parent);

                return;
            }

            if (in_obj instanceof Number) {
                _encodeNumberForKey((Number) in_obj, in_key, in_parent);

                return;
            }

            if (in_obj instanceof Boolean) {
                encodeStringInTag(in_obj.toString(), in_key, in_parent);

                return;
            }

            if (in_obj instanceof EnterpriseObject) {
                _encodeEnterpriseObjectForKey((EnterpriseObject) in_obj, in_parent);

                return;
            }

            if (in_obj instanceof byte[]) {
                _encodeBinaryForKey((byte[]) in_obj, in_key, in_parent);

                return;
            }

            if (_typeConverter != null) {
                encodeObjectForKey(_typeConverter.convert(in_obj), in_key, in_parent);

                return;
            }
        }
    }

    protected String escapeString(String s) {
        int i = s.length();
        StringBuffer stringbuffer = new StringBuffer(i);

        for (int j = 0; j < i; j++) {
            char c = s.charAt(j);

            switch (c) {
            case 38: // '&'
                stringbuffer.append("&amp;");

                break;

            case 60: // '<'
                stringbuffer.append("&lt;");

                break;

            case 62: // '>'
                stringbuffer.append("&gt;");

                break;

            case 39: // '\''
                stringbuffer.append("&apos;");

                break;

            case 34: // '"'
                stringbuffer.append("&quot;");

                break;

            default:
                stringbuffer.append(c);

                break;
            }
        }

        return stringbuffer.toString();
    }

    protected void encodeStringInTag(String in_obj, String in_key, Element in_parent) {
        if (in_obj != null && in_key != null) {
            if (in_obj.trim().length() > 0 || !_disableEmptyString) {
                try {
                    Element l_elt = _document.createElement(in_key);
                    String l_str = in_obj;

                    if ((l_str.indexOf('\n') != -1) || (l_str.indexOf('\t') != -1) || (l_str.indexOf('<') != -1) || (l_str.indexOf('>') != -1) || (l_str.indexOf('\'') != -1) || (l_str.indexOf('"') != -1) || (l_str.indexOf('&') != -1)) {
                        l_elt.appendChild(_document.createCDATASection(l_str));
                    } else {
                        l_elt.appendChild(_document.createTextNode(l_str));
                    }

                    in_parent.appendChild(l_elt);
                } catch (Exception e) {
                    ExceptionUtils.rethrowIfNeeded(e);
                    log.error("encodeString: "+in_obj +" InTag: "+in_key+")", e);
                }
            }
        }
    }

    private void _encodeStringForKey(String in_obj, String in_key, Element in_parent) {
        encodeStringInTag(in_obj, in_key, in_parent);
    }

    private void _encodeBinaryForKey(byte[] in_obj, String in_key, Element in_parent) {
        if (binaryHandler != null) {
            binaryHandler.setContentType(getContentType(in_parent));
            encodeStringInTag(binaryHandler.encode(in_obj), in_key, in_parent);
        } else {
            log.warn(Messages.getMessage("noBinaryHandler00", new String[] { in_key, in_parent != null ? in_parent.toString() : "null" }),
                new IllegalStateException());
        }
    }

    @Override
    public String getContentType(Element in_element) {
        return "application/octet-stream";
    }

    private void _encodeDateForKey(Date in_obj, String in_key, Element in_parent) {
        try {
            encodeStringInTag(_format.format(in_obj), in_key, in_parent);
        } catch (Exception e) {
            ExceptionUtils.rethrowIfNeeded(e);
            log.error(Messages.getMessage("unableToEncode00", new String[] { in_obj != null ? in_obj.toString() : "null", in_key, in_parent != null ? in_parent.toString() : "null" }),e);
        }
    }

    private void _encodeNumberForKey(Number in_obj, String in_key, Element in_parent) {
        encodeStringInTag(in_obj.toString(), in_key, in_parent);
    }

    //private void _encodeNullForKey(String in_key, Element in_parent) {
    //    in_parent.appendChild(_document.createElement(in_key));
    //}

    private void _encodeCollectionForKey(Collection in_obj, String in_key, Element in_parent) {
        if (!in_obj.isEmpty()) {
            Element l_elt = null;

            try {
                if (in_key != null) {
                    l_elt = _document.createElement(in_key);
                } else {
                    l_elt = in_parent;
                }

                for (java.util.Iterator l_list = in_obj.iterator(); l_list.hasNext();) {
                    Object l_obj = l_list.next();

                    if (l_obj instanceof EnterpriseObject) {
                        encodeObjectForKey(l_obj, ((EnterpriseObject) l_obj).name(), l_elt);
                    } else {
                        encodeObjectForKey(l_obj, "element", l_elt);
                    }
                }

                if ((in_key != null) && l_elt.hasChildNodes()) {
                    in_parent.appendChild(l_elt);
                }
            } catch (Exception e) {
                ExceptionUtils.rethrowIfNeeded(e);
                log.error("_encodeCollection: "+in_obj+" ForKey: "+in_key, e);
            }
        }
    }

    private void _encodeDictionaryForKey(Hashtable in_obj, String in_key, Element in_parent) {
        Element l_elt = _document.createElement(in_key);
        String l_key;
        Object obj;

        for (Enumeration l_keys = in_obj.keys(); l_keys.hasMoreElements(); encodeObjectForKey(obj, l_key, l_elt)) {
            l_key = (String) l_keys.nextElement();
            obj = in_obj.get(l_key);
        }

        in_parent.appendChild(l_elt);
    }

    protected Element _rootEntityElement = null;

    private void _encodeEnterpriseObjectForKey(EnterpriseObject in_obj, Element in_parent) {
        String l_entityName = in_obj.name();

        int i = System.identityHashCode(in_obj.baseObject());
        Integer l_hashCode = new Integer(i);

        if (_encodedObjects.containsKey(l_hashCode)) {
            return;
        } else {
            _encodedObjects.put(l_hashCode, in_obj);
        }

        Element l_elt = null;

        if ((_document == null) || (in_parent == null)) {
            _document = _domImpl.createDocument(null, l_entityName, null);
            l_elt = _document.getDocumentElement();
            in_parent = l_elt;
            _rootEntityElement = l_elt;
        } else {
            l_elt = _document.createElement(l_entityName);
            in_parent.appendChild(l_elt);
            if (in_parent.equals(_document.getDocumentElement())) {
                _rootEntityElement = l_elt;
            }
        }

        for (Iterator l_attrKeys = _getKeys(in_obj, 0); l_attrKeys.hasNext();) {
            String l_attr = (String) l_attrKeys.next();

            try {
                Object l_obj = in_obj.valueForKey(l_attr);

                if (l_obj != null) {
                    encodeObjectForKey(l_obj, l_attr, l_elt);
                }
            } catch (InvocationTargetKeyException e) {
                log.warn(Messages.getMessage("invocationTargetKey00", l_attr, getObjNameForDebug(in_obj)), e);
            } catch (UnknownKeyException e) {
                log.warn(Messages.getMessage("invocationTargetKey00", l_attr, getObjNameForDebug(in_obj)), e);
            }
        }

        if (_encodingConfig == null || _encodingConfig.followingRelationships(l_entityName,
                                                                              _rootEntityElement.getNodeName())) {
            for (Iterator l_oneRKeys = _getKeys(in_obj, 1); l_oneRKeys.hasNext();) {
                String l_oneR = (String) l_oneRKeys.next();

                try {
                    Object l_obj = in_obj.valueForKey(l_oneR);

                    if (l_obj != null) {
                        Element l_eltOneR = _document.createElement(l_oneR);
                        encodeObjectForKey(l_obj, l_oneR, l_eltOneR);

                        if (l_eltOneR.hasChildNodes()) {
                            l_elt.appendChild(l_eltOneR);
                        } else {
                            l_eltOneR = null;
                        }
                    }
                } catch (InvocationTargetKeyException e) {
                    log.warn(Messages.getMessage("invocationTargetKey01", l_oneR, getObjNameForDebug(in_obj)), e);
                } catch (UnknownKeyException e) {
                    log.warn(Messages.getMessage("invocationTargetKey01", l_oneR, getObjNameForDebug(in_obj)), e);
                }
            }

            for (Iterator l_manyRKeys = _getKeys(in_obj, 2); l_manyRKeys.hasNext();) {
                String l_manyR = (String) l_manyRKeys.next();

                try {
                    encodeObjectForKey(in_obj.valueForKey(l_manyR), l_manyR, l_elt);
                } catch (InvocationTargetKeyException e) {
                    log.warn(Messages.getMessage("invocationTargetKey02", l_manyR, getObjNameForDebug(in_obj)),
                             e);
                } catch (UnknownKeyException e) {
                    log.warn(Messages.getMessage("invocationTargetKey02", l_manyR, getObjNameForDebug(in_obj)),
                             e);
                }
            }
        }

        _encodedObjects.remove(l_hashCode);
    }

    private String getObjNameForDebug(EnterpriseObject in_obj) {
        return in_obj != null ? in_obj.name() : "null";
    }

    private Hashtable _keyCollectionsCache = new Hashtable();

    private Iterator _getKeys(EnterpriseObject in_obj, int in_type) {
        String l_entityName = in_obj.name();
        Iterator l_result = null;
        String l_cacheKey = l_entityName + in_type;
        Set l_set = null;

        if (useCache && _keyCollectionsCache.containsKey(l_cacheKey)) {
            l_set = (Set) _keyCollectionsCache.get(l_cacheKey);
            l_result = l_set.iterator();
        } else {
            if (_encodingConfig != null) {
                Set l_includes = null;
                Set l_extents = null;
                String l_rootEntityName = _rootEntityElement.getNodeName();

                switch (in_type) {
                case ATTRIBUTE: // 0
                    l_includes = _encodingConfig.getKeys(l_entityName,
                                                         XMLEncodingConfig.INCLUDES,
                                                         XMLEncodingConfig.ATTRIBUTES,
                                                         l_rootEntityName);
                    l_extents = _encodingConfig.getKeys(l_entityName,
                                                        XMLEncodingConfig.EXTENTS,
                                                        XMLEncodingConfig.ATTRIBUTES,
                                                        l_rootEntityName);

                    break;

                case TO_ONE: // 1
                    l_includes = _encodingConfig.getKeys(l_entityName,
                                                         XMLEncodingConfig.INCLUDES,
                                                         XMLEncodingConfig.TO_ONES,
                                                         l_rootEntityName);
                    l_extents = _encodingConfig.getKeys(l_entityName,
                                                        XMLEncodingConfig.EXTENTS,
                                                        XMLEncodingConfig.TO_ONES,
                                                        l_rootEntityName);

                    break;

                case TO_MANY: // 2
                    l_includes = _encodingConfig.getKeys(l_entityName,
                                                         XMLEncodingConfig.INCLUDES,
                                                         XMLEncodingConfig.TO_MANYS,
                                                         l_rootEntityName);
                    l_extents = _encodingConfig.getKeys(l_entityName,
                                                        XMLEncodingConfig.EXTENTS,
                                                        XMLEncodingConfig.TO_MANYS,
                                                        l_rootEntityName);

                    break;
                }

                if (((l_includes != null) && (l_includes.size() > 0)) && ((l_extents != null) && (l_extents.size() > 0))) {
                    l_includes.addAll(l_extents);
                    l_set = l_includes;
                } else if (((l_includes == null) || (l_includes.size() == 0)) && ((l_extents != null) && (l_extents.size() > 0))) {
                    l_extents.addAll(_getObjectKeysForType(in_obj, in_type));
                    l_set = l_extents;
                } else if (((l_includes != null) && (l_includes.size() > 0)) && ((l_extents == null) || (l_extents.size() == 0))) {
                    l_set = l_includes;
                } else {
                    l_set = _getObjectKeysForType(in_obj, in_type);
                }
            } else {
                l_set = _getObjectKeysForType(in_obj, in_type);
            }

            _keyCollectionsCache.put(l_cacheKey, l_set);
            l_result = l_set.iterator();
        }

        return l_result;
    }

    private Set _excludeFilter(Set in_keys, Set in_excludeKeys) {
        HashSet l_keys = new HashSet();
        java.util.Iterator l_list = in_keys.iterator();

        while (l_list.hasNext()) {
            String l_key = (String) l_list.next();

            if (!in_excludeKeys.contains(l_key)) {
                l_keys.add(l_key);
            }
        }

        return l_keys;
    }

    private Set _getObjectKeysForType(EnterpriseObject in_obj, int in_type) {
        Set l_result = null;
        String l_entityName = in_obj.name();
        /*Node l_root = _document.getDocumentElement().getFirstChild();

        if (l_root == null) {
            l_root = _document.getDocumentElement();
        }*/

        String l_rootEntityName = _rootEntityElement.getNodeName();
        Set l_keys;

        switch (in_type) {
        case ATTRIBUTE: // 0
            if (_encodingConfig != null) {
                l_keys = _encodingConfig.getKeys(l_entityName,
                                                 XMLEncodingConfig.EXCLUDES,
                                                 XMLEncodingConfig.ATTRIBUTES,
                                                 l_rootEntityName);

                if (!l_keys.contains(XMLEncodingConfig.ALL_KEYS)) {
                    if (l_keys.size() > 0) {
                        l_result = _excludeFilter(in_obj.keys(), l_keys);
                    } else {
                        l_result = in_obj.keys();
                    }
                }
            } else {
                l_result = in_obj.keys();
            }

            break;

        case TO_ONE: // 1
            if (_encodingConfig != null) {
                l_keys = _encodingConfig.getKeys(l_entityName,
                                                 XMLEncodingConfig.EXCLUDES,
                                                 XMLEncodingConfig.TO_ONES,
                                                 l_rootEntityName);

                if (!l_keys.contains(XMLEncodingConfig.ALL_KEYS)) {
                    if (l_keys.size() > 0) {
                        l_result = _excludeFilter(in_obj.toOneKeys(), l_keys);
                    } else {
                        l_result = in_obj.toOneKeys();
                    }
                }
            } else {
                l_result = in_obj.toOneKeys();
            }

            break;

        case TO_MANY: // 2
            if (_encodingConfig != null) {
                l_keys = _encodingConfig.getKeys(l_entityName,
                                                 XMLEncodingConfig.EXCLUDES,
                                                 XMLEncodingConfig.TO_MANYS,
                                                 l_rootEntityName);

                if (!l_keys.contains(XMLEncodingConfig.ALL_KEYS)) {
                    if (l_keys.size() > 0) {
                        l_result = _excludeFilter(in_obj.toManyKeys(), l_keys);
                    } else {
                        l_result = in_obj.toManyKeys();
                    }
                }
            } else {
                l_result = in_obj.toManyKeys();
            }

            break;
        }

        if (l_result == null) {
            l_result = new HashSet();
        }

        return l_result;
    }
}
