package fr.improve.xdi;

import com.resurgences.utils.ExceptionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import fr.improve.xdi.mapping.EnterpriseContext;
import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.exception.EnterpriseContextException;
import fr.improve.xdi.mapping.exception.InvocationTargetKeyException;
import fr.improve.xdi.mapping.exception.UnknownKeyException;
import fr.improve.xdi.resources.Messages;

/**
 * Default implementation of XMLEncoder
 *
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class DefaultXMLDecoder implements XMLDecoder {
     static final String NOW_KEY = "#NOW";

    protected String logName = fr.improve.xdi.XMLDecoder.class.getName();

    protected Log log = LogFactory.getLog(fr.improve.xdi.XMLDecoder.class.getName());

    protected Stack parentTree;

    private SimpleDateFormat _dateFormat = new SimpleDateFormat(Integrator.DEFAULT_DATE_FORMAT);

    private SimpleDateFormat _decimalFormat = new SimpleDateFormat(Integrator.DEFAULT_DECIMAL_FORMAT);

    final protected EnterpriseContext enterpriseContext;

    protected IntegrateErrorHandler errorHandler = null;

    protected BinaryHandler binaryHandler = null;

    private Class<? extends IntegrateHandler> _defaultIntegrateHandler;

    private String _dateFormatPattern = Integrator.DEFAULT_DATE_FORMAT;

    private String _decimalFormatPattern = Integrator.DEFAULT_DECIMAL_FORMAT;

    @Inject
    private  Injector injector;

    @Inject
    public DefaultXMLDecoder(EnterpriseContext in_enterpriseContext,
                             @Named("IntegrateHandler") Class in_defaultIntegrateHandler) {
        parentTree = new Stack();
        enterpriseContext = in_enterpriseContext;
        _defaultIntegrateHandler = in_defaultIntegrateHandler;
        _dateFormat.setLenient(false);
    }

    @Override
    public void setLogName(String in_name) {
        logName = in_name;
        log = LogFactory.getLog(in_name);
        if (errorHandler != null) errorHandler.setLogName(in_name);
    }

    @Override
    public void setLog(Log in_log) {
        logName = fr.improve.xdi.XMLDecoder.class.getName();
        log = in_log;
        if (errorHandler != null) errorHandler.setLog(in_log);
    }

    @Override
    public EnterpriseContext getEnterpriseContext() {
        return enterpriseContext;
    }

    @Override
    public void setDateFormat(String in_pattern) {
        _dateFormatPattern = in_pattern;
    }
    
    @Override
    public String getDateFormat() {
        return _dateFormatPattern;
    }
    
    @Override
    public void setErrorHandler(IntegrateErrorHandler in_errorHandler) {
        errorHandler = in_errorHandler;
    }

    @Override
    public void setDefaultIntegrateHandler(Class<? extends IntegrateHandler> in_defaultIntegrateHandler) {
        _defaultIntegrateHandler = in_defaultIntegrateHandler;
    }
    
    @Override
    public IntegrateErrorHandler errorHandler() {
        return errorHandler;
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
    public Object decodeObjectForNode(Node in_node) throws DecodeException, IntegrateException {
        switch (in_node.getNodeType()) {
        case Node.DOCUMENT_NODE:
            return decodeObjectForNode(((Node) (((Document) in_node).getDocumentElement())));

        case Node.ELEMENT_NODE:

            Element l_elt = (Element) in_node;
            String l_type = l_elt.getAttribute(Integrator.TYPE_KEY);

            if (Integrator.TO_ONE_RELATIONSHIP.equals(l_type)) {
                for (int i=0; i<in_node.getChildNodes().getLength(); i++) {
                    if (in_node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                        return decodeObjectForNode(in_node.getChildNodes().item(i));
                    }
                }
            }

            if (Integrator.ENTERPRISE_OBJECT.equals(l_type)) {
                return _decodeRecord(l_elt);
            }

            if (Integrator.BINARY.equals(l_type)) {
                return _decodeBinary(l_elt);
            }

            if ((l_type == null) || (l_type.trim().length() == 0)) {
                l_type = "java.lang.String";
            }

            Class l_objClass;

            try {
                l_objClass = Class.forName(l_type);
            } catch (ClassNotFoundException e) {
                if (l_type.equals("byte[]")) {
                    return _decodeByteArray(l_elt);
                } else if (l_type.equals(Boolean.TYPE.getName())) {
                    // TODO : ajouter tout les types primitifs
                    return _decodeBoolean(l_elt, Boolean.class);
                } else {
                    throw new DecodeException(Messages.getMessage("unknownType00", l_type), e);
                }
            }

            if (java.lang.String.class.isAssignableFrom(l_objClass)) {
                return _decodeString(l_elt, l_objClass, l_elt.getAttribute(Integrator.CASE_KEY));
            }

            if (java.util.Date.class.isAssignableFrom(l_objClass)) {
                String l_dateFormat = l_elt.getAttribute(Integrator.FORMAT_KEY);

                if ((l_dateFormat != null) && (l_dateFormat.length() > 0)) {
                    _dateFormat.applyPattern(l_dateFormat);
                } else {
                    _dateFormat.applyPattern(_dateFormatPattern);
                }

                return _decodeDate(l_elt, l_objClass);
            }

            if (java.util.Collection.class.isAssignableFrom(l_objClass)) {
                return _decodeCollection(l_elt, l_objClass);
            }

            if (java.lang.Number.class.isAssignableFrom(l_objClass)) {
                String l_numFormat = l_elt.getAttribute(Integrator.FORMAT_KEY);

                if ((l_numFormat != null) && (l_numFormat.length() > 0)) {
                    _decimalFormat.applyPattern(l_numFormat);
                } else {
                    _decimalFormat.applyPattern(_decimalFormatPattern);
                }
                return _decodeNumber(l_elt, l_objClass);
            }

            if (java.lang.Boolean.class.isAssignableFrom(l_objClass)) {
                return _decodeBoolean(l_elt, l_objClass);
            }

            break;
        }

        return null;
    }

    private Object _decodeBoolean(Element in_element, Class in_class) throws DecodeException {
        Object l_obj = null;
        Node l_node = in_element.getFirstChild();

        if (l_node != null) {
            switch (l_node.getNodeType()) {
            case Node.TEXT_NODE:

                String l_str = ((Text) l_node).getData().trim().replace(',', '.');

                if (l_str.length() == 0) {
                    return null;
                }

                try {
                    Constructor constructor = in_class.getConstructor(new Class[] { String.class });
                    l_obj = constructor.newInstance(new Object[] { l_str });
                } catch (NoSuchMethodException e) {
                    throw new DecodeException(Messages.getMessage("unknownConstructor00",
                                                                  in_class.getName(),
                                                                  "(java.lang.String)"), e);
                } catch (Exception e) {
                    throw new DecodeException(Messages.getMessage("unableToDecode00",
                                                                  in_class.getName()), e);
                }

                break;
            }
        }

        return l_obj;
    }

    private Object _decodeNumber(Element in_element, Class in_class) throws DecodeException {
        Object l_obj = null;
        Node l_node = in_element.getFirstChild();

        if (l_node != null) {
            switch (l_node.getNodeType()) {
            case Node.TEXT_NODE:

                String l_str = ((Text) l_node).getData().trim();

                if (l_str.length() == 0) {
                    return null;
                }

                try {
                    //TODO : utiliser le format
                    Constructor constructor = in_class.getConstructor(new Class[] { String.class });
                    l_obj = constructor.newInstance(new Object[] { l_str });
                } catch (NoSuchMethodException e) {
                    throw new DecodeException(Messages.getMessage("unknownConstructor00",
                                                                  in_class.getName(),
                                                                  "(java.lang.String)"), e);
                } catch (Exception e) {
                    throw new DecodeException(Messages.getMessage("unableToDecode00",
                                                                  in_class.getName()), e);
                }

                break;
            }
        }

        return l_obj;
    }

    private Object _decodeBinary(Element in_element) throws DecodeException {
        Object l_obj = null;
        Node l_node = in_element.getFirstChild();

        if (l_node != null) {
            switch (l_node.getNodeType()) {
            case Node.TEXT_NODE:

                String l_str = ((Text) l_node).getData().trim();

                if (l_str.length() == 0) {
                    return null;
                }

                try {
                    if (binaryHandler != null) {
                        l_obj = binaryHandler.decode(l_str);
                        setContentType((Element) in_element.getParentNode(),
                                       binaryHandler.getContentType());
                    } else {
                        log.warn(Messages.getMessage("noBinaryHandler00"));
                    }
                } catch (Exception e) {
                    throw new DecodeException(Messages.getMessage("unableToDecode00",
                                                                  "binary : " + l_str), e);
                }

                break;
            }
        }

        return l_obj;
    }

    public void setContentType(Element in_element, String in_contentType) {
        // implement specific treatment to use the contentType
    }

    private Object _decodeByteArray(Element in_element) {
        NodeList l_list = in_element.getChildNodes();
        StringBuffer l_data = new StringBuffer();

        for (int i = 0; i < l_list.getLength(); i++) {
            Node l_node = l_list.item(i);

            if (l_node != null) {
                switch (l_node.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    l_data.append(((Text) l_node).getData());

                    break;
                }
            }
        }

        return l_data.toString().getBytes();
    }

    private Object _decodeString(Element in_element, Class in_class, String in_case)
            throws DecodeException {
        Object l_obj = null;
        Node l_node = in_element.getFirstChild();

        if (l_node != null) {
            switch (l_node.getNodeType()) {
            case Node.TEXT_NODE:

                String l_str = ((Text) l_node).getData().trim();

                if (in_case.equals(Integrator.UPPER_CASE)) {
                    l_str = l_str.toUpperCase();
                } else if (in_case.equals(Integrator.LOWER_CASE)) {
                    l_str = l_str.toLowerCase();
                }

                if (l_str.length() == 0) {
                    return null;
                }

                try {
                    Constructor l_constructor = in_class.getConstructor(new Class[] { String.class });
                    Object[] l_aobj = new Object[1];
                    l_aobj[0] = l_str;
                    l_obj = l_constructor.newInstance(l_aobj);
                } catch (NoSuchMethodException e) {
                    throw new DecodeException(Messages.getMessage("unknownConstructor00",
                                                                  in_class.getName(),
                                                                  "(java.lang.String)"), e);
                } catch (Exception e) {
                    throw new DecodeException(Messages.getMessage("unableToDecode00",
                                                                  in_class.getName()), e);
                }

                break;
            }
        }

        return l_obj;
    }

    protected Object _decodeDate(Element in_element, Class in_class) throws DecodeException {
        Object l_obj = null;
        Node l_node = in_element.getFirstChild();

        if (l_node != null) {
            switch (l_node.getNodeType()) {
            case Node.TEXT_NODE:

                String l_str = ((Text) l_node).getData().trim();

                if (l_str.length() == 0) {
                    return null;
                }

                try {
                    Date l_date = null;
                    if (l_str.equals(NOW_KEY)) {
                        l_date = new Date();
                    } else {
                        l_date = _dateFormat.parse(l_str, new ParsePosition(0));
                    }

                    if (l_date != null) {
                        Constructor l_constructor = null;

                        try {
                            l_constructor = in_class.getConstructor(new Class[] { Date.class });
                            l_obj = l_constructor.newInstance(new Object[] { l_date });
                        } catch (NoSuchMethodException e1) {
                            l_constructor = in_class.getConstructor(new Class[] { Long.TYPE });
                            l_obj = l_constructor.newInstance(new Object[] { new Long(l_date.getTime()) });
                        }
                    } else {
                        throw new DecodeException(Messages.getMessage("unableToParseDate00", l_str, _dateFormat.toPattern()));
                    }
                } catch (NoSuchMethodException e) {
                    throw new DecodeException(Messages.getMessage("unknownConstructor00",
                                                                  in_class.getName(),
                                                                  "(java.lang.Long)"), e);
                } catch (Exception e) {
                    ExceptionUtils.rethrowIfNeeded(e);
                    if (!(e instanceof DecodeException)) {
                        throw new DecodeException(Messages.getMessage("unableToDecode00",
                                                                      in_class.getName()), e);
                    } else {
                        throw (DecodeException)e;
                    }
                }

                break;
            }
        }

        return l_obj;
    }

    private Collection _decodeCollection(Element in_element, Class in_class)
            throws DecodeException, IntegrateException {
        Collection l_array = null;
        NodeList l_list = in_element.getChildNodes();
        int l_length = l_list.getLength();

        try {
            Constructor l_constructor = in_class.getConstructor(new Class[0]);
            l_array = (Collection) l_constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            ExceptionUtils.rethrowIfNeeded(e);
            try {
                Constructor l_constructor = in_class.getConstructor(new Class[0]);
                l_array = (Collection) l_constructor.newInstance(new Object[] { new Integer(l_length) });
            } catch (Exception e1) {
                throw new DecodeException(Messages.getMessage("unableToDecode00",
                                                              in_class.getName()), e1);
            }
        }

        for (int i = 0; i < l_length; i++) {
            Node l_node = l_list.item(i);

            if (l_node.getNodeType() == 1) {
                Object l_obj = decodeObjectForNode(l_node);

                if (l_obj != null) {
                    l_array.add(l_obj);
                }
            }
        }

        return l_array;
    }

    /**
     * Decode XML object
     *
     * @param in_element
     * @return
     * @throws DecodeException
     * @throws IntegrateException
     */
    private Object _decodeRecord(Element in_element) throws DecodeException, IntegrateException {
        EnterpriseObject l_parent = null;

        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("beginDecode00", in_element.getLocalName()));
        }

        try {
            l_parent = (EnterpriseObject) parentTree.peek();
        } catch (EmptyStackException e) {
            // ignore
        }

        IntegrateHandler l_handler = null;

        try {
            l_handler = (IntegrateHandler) newIntegrateHandler(in_element, l_parent);
            l_handler.setErrorHandler(errorHandler);
            l_handler.setLogName(logName);
        } catch (Exception e) {
            throw new DecodeException(Messages.getMessage("unableToInstanciate00", l_handler != null ? l_handler.getClass().getName() : "null"), e);
        }

        EnterpriseObject l_object = null;

        l_handler.validate();
        l_object = l_handler.search();

        if (!l_handler.searchOnly()) {
            l_object = l_handler.willBindValues(l_object);

            boolean hasObject = l_object != null;

            if (hasObject) {
                parentTree.push(l_object);

                NodeList l_list = in_element.getChildNodes();

                for (int i = 0; i < l_list.getLength();) {
                    Node l_node = l_list.item(i);

                    switch (l_node.getNodeType()) {
                    case Node.ELEMENT_NODE:

                        try {
                            String l_name = ((Element) l_node).getLocalName();
                            Object l_decodedObject = decodeObjectForNode(l_node);
                            String l_action = ((Element) l_node).getAttribute(Integrator.ACTION_KEY);

                            if ((l_decodedObject != null) || (l_action == null) || !l_action.equals(Integrator.SEARCH)) {
                                if (l_handler.willTakeValueForKey(l_decodedObject, l_name)) {
                                    try {
                                        // toManyRelationship case
                                        if ((l_decodedObject != null) && (l_decodedObject instanceof Collection)) {
                                            // if action == REPLACE then delete current objects in the toMany array
                                            if ((l_action != null) && l_action.equals(Integrator.REPLACE)) {
                                                Collection l_currentValue = ((Collection) l_object.valueForKey(l_name));
                                                try {
                                                    EnterpriseObject[] l_values = (EnterpriseObject[]) l_currentValue.toArray(new EnterpriseObject[l_currentValue.size()]);

                                                    for (int j = 0; j < l_values.length; j++) {
                                                        EnterpriseObject l_obj = l_values[j];

                                                        l_object.removeObjectFromBothSidesOfRelationshipWithKey(l_obj,
                                                                                                                l_name);

                                                        String l_cascade = ((Element) l_node).getAttribute(Integrator.CASCADE_KEY);

                                                        if ((l_cascade != null)
                                                                && Boolean.valueOf(l_cascade).booleanValue()) {
                                                            try {
                                                                enterpriseContext.delete(l_obj);
                                                            } catch (EnterpriseContextException e) {
                                                                log.error(Messages.getMessage("cascadeDeleteFailed00"),
                                                                          e);
                                                            }
                                                        }
                                                    }
                                                } catch (ClassCastException e1) {
                                                    log.error(Messages.getMessage("toManyRelationshipArray00",
                                                                                  new String[] {l_currentValue.toString()}),
                                                              e1);
                                                }
                                            }

                                            Iterator l_iterator = ((Collection) l_decodedObject).iterator();

                                            while (l_iterator.hasNext()) {
                                                l_object.addObjectToBothSidesOfRelationshipWithKey((EnterpriseObject) l_iterator.next(),
                                                                                                   l_name);
                                            }
                                        } else {
                                            l_object.takeValueForKey(l_decodedObject, l_name);
                                        }
                                    } catch (InvocationTargetKeyException e) {
                                        throw new DecodeException(e);
                                    } catch (UnknownKeyException e) {
                                        throw new DecodeException(e);
                                    } catch (ClassCastException e) {
                                        log.error(Messages.getMessage("toManyRelationshipName00",
                                                                      new String[] { l_name }), e);
                                        throw new DecodeException(e);
                                    }
                                }

                                l_handler.didTakeValueForKey(l_decodedObject, l_name);
                            }
                        } catch (DecodeException e1) {
                            if ((errorHandler != null) && !errorHandler.continueAfterDecodeException(e1)) {
                                throw e1;
                            } else {
                                log.warn("Erreur de decodage", e1);
                            }
                        }

                    default:
                        i++;

                        break;
                    }
                }
            }

            l_handler.didBindValues(l_object);

            if (hasObject) {
                parentTree.pop();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("endDecode00", in_element.getLocalName()));
        }

        return l_object;
    }

    public IntegrateHandler newIntegrateHandler(Element in_element, EnterpriseObject in_parentObject)
            throws InstantiationException, IllegalAccessException {
        String l_handlerType = in_element.getAttribute(Integrator.HANDLER_KEY);
        IntegrateHandler l_handler = null;
        Class l_class;

        try {
            if ((l_handlerType == null) || (l_handlerType.trim().length() == 0)) {
                l_class = _defaultIntegrateHandler;
            } else {
                try {
                    l_class = Class.forName(l_handlerType);
                } catch (ClassNotFoundException e1) {
                    l_class = _defaultIntegrateHandler;
                }
            }

                l_handler = createHandler(in_element, in_parentObject, l_class);
            
        } catch (InvocationTargetException e) {
            log.error("Failure on element '" + in_element + "', parent '" + in_parentObject + "'.", e);
            throw new InstantiationException(e.getTargetException().getMessage());
        }catch (NoSuchMethodException e) {
            log.error("Failure on element '" + in_element + "', parent '" + in_parentObject + "'.", e);
            throw new RuntimeException(e);
        }

        l_handler.setDecoder(this);
        return l_handler;
    }

    protected IntegrateHandler createHandler(Element in_element,
            EnterpriseObject in_parentObject, Class l_class)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        IntegrateHandler l_handler;
        Constructor l_constructor = l_class.getConstructor(new Class[] { EnterpriseContext.class, Element.class, EnterpriseObject.class });
        l_handler = (IntegrateHandler) l_constructor.newInstance(new Object[] { getEnterpriseContext(), in_element, in_parentObject });
        injector.injectMembers(l_handler);
        return l_handler;
    }

 
}
