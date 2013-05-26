package fr.improve.xdi;

import com.resurgences.utils.ExceptionUtils;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.improve.xdi.mapping.EnterpriseContext;
import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.exception.EnterpriseContextException;
import fr.improve.xdi.resources.Messages;

/**
 * Default implementation of IntegrateHandler
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class DefaultIntegrateHandler implements IntegrateHandler {
    protected Log log = LogFactory.getLog(DefaultIntegrateHandler.class.getName());
    protected EnterpriseContext enterpriseContext;
    protected Element element;
    protected String action;
    protected boolean objectCreated;
    protected EnterpriseObject parent;
    protected IntegrateErrorHandler errorHandler = null;
    protected XMLDecoder decoder = null;

    public DefaultIntegrateHandler(EnterpriseContext in_enterpriseContext, Element in_element,
                                   EnterpriseObject in_parentObject) {
        objectCreated = false;
        enterpriseContext = in_enterpriseContext;
        element = in_element;
        action = in_element.getAttribute(Integrator.ACTION_KEY);
        parent = in_parentObject;

        if ((action == null) || (action.trim().length() == 0)) {
            action = Integrator.CREATE_OR_UPDATE;
        }
    }

    @Override
    public void setLogName(String in_name) {
        log = LogFactory.getLog(in_name);
        if (errorHandler != null) errorHandler.setLogName(in_name);
    }

    @Override
    public void setLog(Log in_log) {
        log = in_log;
        if (errorHandler != null) errorHandler.setLog(in_log);
    }

    @Override
    public void setErrorHandler(IntegrateErrorHandler in_errorHandler) {
        errorHandler = in_errorHandler;
    }

    @Override
    public IntegrateErrorHandler errorHandler() {
        return errorHandler;
    }

    @Override
    public void setDecoder(XMLDecoder in_decoder) {
        decoder = in_decoder;
    }

    public XMLDecoder decoder() {
        return decoder;
    }

    // TODO : attention le renvoit d'un IntegrateException si il y a un commitIteration > 1
    // pose probléme car l'objet invalide entraine la non sauvegarde des autres objets de la meme transaction
    @Override
    public void validate() throws IntegrateException {
    }

    public String valueWithXPath(String in_xpath) {
        try {
            Node l_node = XPathAPI.selectSingleNode(element, in_xpath);

            if (l_node != null) {
                if (l_node.getFirstChild() != null) {
                    return l_node.getFirstChild().getNodeValue();
                }
            }
        } catch (TransformerException e) {
            log.debug(Messages.getMessage("xpathFailed00"), e);
        }

        return null;
    }

    @Override
    public boolean searchOnly() {
        return element.getAttribute(Integrator.ACTION_KEY).equals(Integrator.SEARCH);
    }

    public Hashtable getCriterias() throws TransformerException {
        NodeList l_list = XPathAPI.selectNodeList(element, Integrator.SEARCH_XPATH);

        if (l_list.getLength() > 0) {
            Hashtable l_values = new Hashtable();

            for (int i = 0; i < l_list.getLength(); i++) {
                Element l_node = (Element) l_list.item(i);
                try {
                    Object l_value = decoder.decodeObjectForNode(l_node);
                    if (l_value != null) {
                        l_values.put(l_node.getLocalName(), l_value);
                    }
                } catch (Exception e) {
                    ExceptionUtils.rethrowIfNeeded(e);
                    log.warn("Decode error",e);
                }

                /*
                 * try { String l_str = null; Node l_child =
                 * l_node.getFirstChild();
                 * 
                 * if (l_child != null) { l_str = l_child.getNodeValue(); }
                 * 
                 * Object l_value = null; String l_typeStr =
                 * l_node.getAttribute(Integrator.TYPE_KEY);
                 * 
                 * if ((l_typeStr == null) || (l_typeStr.trim().length() == 0)) {
                 * l_typeStr = "java.lang.String"; }
                 * 
                 * Class l_type = Class.forName(l_typeStr);
                 * 
                 * if ((l_str == null) || (l_str.trim().length() == 0)) {
                 * l_value = null; } else if
                 * (java.lang.String.class.isAssignableFrom(l_type)) { l_value =
                 * l_str; } else if
                 * (java.lang.Number.class.isAssignableFrom(l_type)) { l_value =
                 * l_type.getConstructor(new Class[] { java.lang.String.class
                 * }).newInstance( new Object[] { l_str }); } else { if
                 * (!java.util.Date.class.isAssignableFrom(l_type) &&
                 * !java.sql.Timestamp.class.isAssignableFrom(l_type)) {
                 * continue; }
                 * 
                 * String l_format = l_node.getAttribute(Integrator.FORMAT_KEY);
                 * 
                 * if (l_format == null) { continue; }
                 * 
                 * try { SimpleDateFormat l_formatter = new
                 * SimpleDateFormat(l_format); l_value =
                 * l_type.getConstructor(new Class[] { Long.TYPE }).newInstance(
                 * new Object[] { new Long(l_formatter.parse(l_str).getTime())
                 * }); } catch (Exception e) { continue; } }
                 * 
                 * l_values.put(l_node.getLocalName(), l_value); } catch
                 * (Exception e) { log.warn(e); }
                 */
            }

            return l_values;
        } else {
            return null;
        }
    }
    
    @Override
    public EnterpriseObject search() throws IntegrateException {
        EnterpriseObject l_result = null;
        log.debug(Messages.getMessage("search00", element.getLocalName()));
        try {
            Hashtable l_values = getCriterias();

            if ((l_values != null) && (l_values.size() > 0)) {
                Collection l_array = enterpriseContext.objectsWithClassNamedBindingValues(element.getLocalName(),
                                                                                          l_values);
                if (!l_array.isEmpty()) {
                    l_result = selectObjectInFindedObjects((List) l_array);
                }
                if (log.isDebugEnabled()) {
                    if (l_result == null) {
                        log.debug(Messages.getMessage("notFoundObject00", element.getLocalName(), l_values.toString()));
                    } else {
                        log.debug(Messages.getMessage("foundObject00", element.getLocalName(), l_values.toString()));
                    }
                }
            } else {
                log.debug(Messages.getMessage("noSearch00"));
            }
        } catch (TransformerException e) {
            throw new IntegrateException(e);
        } catch (EnterpriseContextException e) {
            throw new IntegrateException(e);
        }

        return l_result;
    }

    @Override
    public EnterpriseObject selectObjectInFindedObjects(List in_findedObjects) {
        try {
            if (in_findedObjects.size() > 0) {
                return (EnterpriseObject) in_findedObjects.get(in_findedObjects.size()-1);
            }
        } catch (NoSuchElementException e) {
            log.warn("selectObjectInFindedObjects error",e);
        } catch (IndexOutOfBoundsException e) {
            log.warn("selectObjectInFindedObjects error",e);
        }

        return null;
    }

    @Override
    public EnterpriseObject willBindValues(EnterpriseObject in_object) throws IntegrateException {
        if (in_object == null && !element.hasChildNodes()) {
            objectCreated = false;
            log.warn(Messages.getMessage("noXMLDataForElement00"));
        } else if ((in_object == null) && (Integrator.CREATE.equals(action) || Integrator.CREATE_OR_UPDATE.equals(action))) {
            in_object = create();
        } else if ((in_object != null) && Integrator.CREATE.equals(action)) {
            log.debug(Messages.getMessage("noAction01"));
            in_object = null;
            objectCreated = false;
        } else if ((in_object != null) && Integrator.DELETE.equals(action)) {
            in_object = delete(in_object);
            objectCreated = false;
        } else {
            if (log.isDebugEnabled()) {
                if (!Integrator.CREATE.equals(action) && in_object == null) {
                    log.debug(Messages.getMessage("noAction00"));
                }
            }
            objectCreated = false;
        }

        return in_object;
    }

    public EnterpriseObject create() throws IntegrateException {
        EnterpriseObject l_result = null;
        try {
            log.debug(Messages.getMessage("createAction00"));
            l_result = enterpriseContext.createInstance(element.getLocalName());
            objectCreated = true;
        } catch (EnterpriseContextException e) {
            throw new IntegrateException(e);
        }
        return l_result;
    }

    public EnterpriseObject delete(EnterpriseObject in_object) throws IntegrateException {
        try {
            log.debug(Messages.getMessage("deleteAction00"));
            enterpriseContext.delete(in_object);
        } catch (EnterpriseContextException e) {
            throw new IntegrateException(e);
        }
        return null;
    }

    @Override
    public boolean willTakeValueForKey(Object in_value, String in_key) {
        return !action.equals(Integrator.CREATE) || objectCreated;
    }

    @Override
    public void didTakeValueForKey(Object in_value, String in_key) {
    }

    @Override
    public void didBindValues(EnterpriseObject in_enterpriseObject) throws IntegrateException {
        if (in_enterpriseObject != null) {
            try {
                enterpriseContext.update(in_enterpriseObject);
            } catch (EnterpriseContextException e) {
                throw new IntegrateException(e);
            }
        }
    }
}
