package fr.improve.xdi.mapping.eof;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import fr.improve.xdi.mapping.AbstractEnterpriseContext;
import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.SortOrdering;
import fr.improve.xdi.mapping.exception.EnterpriseContextException;

/***
 * EnterpriseContext implementation for EOF
 * Equivalent object : EOEditingContext
 *
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class EnterpriseContextImpl extends AbstractEnterpriseContext {
    protected static Log log = LogFactory.getLog(EnterpriseContextImpl.class);

    private EOEditingContext _editingContext;

    public EnterpriseContextImpl() {
    }

    public EnterpriseContextImpl(EOEditingContext in_editingContext) {
        _editingContext = in_editingContext;
    }

    public void setEditingContext(EOEditingContext in_editingContext) {
        _editingContext = in_editingContext;
    }

    public EOEditingContext getEditingContext() {
        return _editingContext;
    }

    public EnterpriseObject createInstance(String in_className) {
        EnterpriseObjectImpl l_eo = null;

        l_eo = new EnterpriseObjectImpl(EOUtilities.createAndInsertInstance(_editingContext,
                                                                            in_className));

        return l_eo;
    }

    public EnterpriseObject createAndInsertInstance(String s) throws EnterpriseContextException {
        return createInstance(s);
    }

    public EnterpriseObject objectWithID(String in_name, Object in_primaryKey)
            throws EnterpriseContextException {
        EnterpriseObjectImpl l_eo = null;

        l_eo = new EnterpriseObjectImpl(EOUtilities.objectWithPrimaryKeyValue(_editingContext,
                                                                              in_name,
                                                                              in_primaryKey));

        return l_eo;
    }

    public void delete(EnterpriseObject in_object) {
        _editingContext.deleteObject((EOEnterpriseObject) in_object.baseObject());
    }

    public void insert(EnterpriseObject in_object) {
        _editingContext.insertObject((EOEnterpriseObject) in_object.baseObject());
    }

    public void update(EnterpriseObject enterpriseobject) {
    }

    public void invalidateAllObjects() {
        //_editingContext.invalidateAllObjects();
        //_editingContext.rootObjectStore().invalidateAllObjects();
        _editingContext.revert();
    }

    public void saveChanges() throws EnterpriseContextException {
        log.debug("insertedObjects : " + _editingContext.insertedObjects().count());
        log.debug("updatedObjects : " + _editingContext.updatedObjects().count());
        log.debug("deletedObjects : " + _editingContext.deletedObjects().count());
        try {
            if (enterpriseContextHandler != null) {
                enterpriseContextHandler.willSaveChanges(this);
            }
            _editingContext.saveChanges();
            _editingContext.undoManager().removeAllActions();
            if (enterpriseContextHandler != null) {
                enterpriseContextHandler.didSaveChanges(this);
            }
        } catch (Exception e) {
            throw new EnterpriseContextException(e);
        }

    }

    public Collection objectsWithClassNamed(String in_name) throws EnterpriseContextException {
        NSArraySet l_results = null;

        l_results = new NSArraySet(EOUtilities.objectsForEntityNamed(_editingContext, in_name));

        return l_results;
    }

    public Collection objectsWithClassNamedBindingLikeValues(String in_name, Map in_values)
            throws EnterpriseContextException {
        // TODO objectsWithClassNamedBindingLikeValues
        return null;
    }

    public Collection objectsWithClassNamedBindingValue(String in_name,
                                                        String in_key,
                                                        Object in_value)
            throws EnterpriseContextException {
        NSArraySet l_results = null;

        l_results = new NSArraySet(EOUtilities.objectsMatchingKeyAndValue(_editingContext,
                                                                          in_name,
                                                                          in_key,
                                                                          in_value));

        return l_results;
    }

    public Collection objectsWithClassNamedBindingValues(String in_name, Map in_values)
            throws EnterpriseContextException {
        NSArraySet l_results = null;

        l_results = new NSArraySet(EOUtilities.objectsMatchingValues(_editingContext,
                                                                     in_name,
                                                                     _convertToDictionary(in_values)));

        return l_results;
    }

    /*
     * @see fr.improve.xdi.mapping.EnterpriseContext#objectsWithClassNamed(java.lang.String, java.util.Set)
     */
    public Collection objectsWithClassNamed(String in_name, Set in_orders)
            throws EnterpriseContextException {
        return objectsWithClassNamedBindingValues(in_name, null, in_orders);
    }

    /*
     * @see fr.improve.xdi.mapping.EnterpriseContext#objectsWithClassNamedBindingValues(java.lang.String, java.util.Map, java.util.Set)
     */
    public Collection objectsWithClassNamedBindingValues(String in_name,
                                                         Map in_values,
                                                         Set in_orders)
            throws EnterpriseContextException {
        EOQualifier l_qualifier = null;
        NSArraySet l_results = null;

        if (in_values != null) {
            l_qualifier = EOQualifier.qualifierToMatchAllValues(_convertToDictionary(in_values));
        }
        NSMutableArray l_sortOrderings = null;
        if (in_orders != null) {
            l_sortOrderings = new NSMutableArray();
            for (Iterator l_keys = in_orders.iterator(); l_keys.hasNext();) {
                l_sortOrderings.addObject((EOSortOrdering) ((SortOrdering) l_keys.next()).getOrder());
            }
        }
        EOFetchSpecification l_fetch = new EOFetchSpecification(in_name,
                                                                l_qualifier,
                                                                l_sortOrderings);
        l_results = new NSArraySet(_editingContext.objectsWithFetchSpecification(l_fetch));

        return l_results;
    }

    public void endTransaction() throws EnterpriseContextException {
        _editingContext.unlock();
        log.debug("endTransaction (unlock)");
    }

    public void beginTransaction() throws EnterpriseContextException {
        log.debug("beginTransaction (lock)");
        _editingContext.lock();
    }

    private NSDictionary _convertToDictionary(Map in_map) {
        NSMutableDictionary l_dict = new NSMutableDictionary();
        for (Iterator l_keys = in_map.keySet().iterator(); l_keys.hasNext();) {
            String l_key = (String) l_keys.next();
            l_dict.setObjectForKey(in_map.get(l_key), l_key);
        }
        return l_dict;
    }

    /*
     * @see fr.improve.xdi.mapping.EnterpriseContext#sortOrderings(java.lang.String, int)
     */
    public Set sortOrderings(String in_key, int in_type) {
        HashSet l_result = new HashSet();
        l_result.add(new SortOrderingImpl(in_key, in_type));
        return l_result;
    }

    /*
     * @see fr.improve.xdi.mapping.EnterpriseContext#sortOrderings(java.util.Map)
     */
    public Set sortOrderings(Map in_keyTypes) {
        Iterator l_keys = in_keyTypes.keySet().iterator();
        HashSet l_result = new HashSet();
        while (l_keys.hasNext()) {
            String l_key = (String) l_keys.next();
            l_result.add(new SortOrderingImpl(l_key, ((Integer) in_keyTypes.get(l_key)).intValue()));
        }
        return l_result;
    }

}
