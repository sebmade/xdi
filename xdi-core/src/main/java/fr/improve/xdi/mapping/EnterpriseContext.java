package fr.improve.xdi.mapping;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.improve.xdi.mapping.exception.EnterpriseContextException;

/**
 * Interface implemented for each mapping tool
 * Represents the context of the database
 * Used to call requests, commit, rollback, create, insert or delete database objects
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface EnterpriseContext extends Serializable {
    public void setEnterpriseContextHandler(EnterpriseContextHandler in_handler);

    public EnterpriseContextHandler enterpriseContextHandler();

    public EnterpriseObject createAndInsertInstance(String in_entityName) throws EnterpriseContextException;

    public EnterpriseObject createInstance(String in_entityName) throws EnterpriseContextException;

    public Set sortOrderings(String in_key, int in_type);

    public Set sortOrderings(Map in_keyTypes);

    public EnterpriseObject objectWithID(String in_name, Object in_primaryKey) throws EnterpriseContextException;

    public Collection objectsWithClassNamed(String in_entityName) throws EnterpriseContextException;

    public Collection objectsWithClassNamed(String in_entityName, Set in_orders) throws EnterpriseContextException;

    public Collection objectsWithClassNamedBindingValue(String in_entityName, String in_key, Object in_value) throws EnterpriseContextException;

    public Collection objectsWithClassNamedBindingValues(String in_entityName, Map in_bindings) throws EnterpriseContextException;

    public Collection objectsWithClassNamedBindingValues(String in_entityName, Map in_bindings, Set in_orders) throws EnterpriseContextException;

    public Collection objectsWithClassNamedBindingLikeValues(String in_entityName, Map in_bindings) throws EnterpriseContextException;

    public void beginTransaction() throws EnterpriseContextException;

    public void endTransaction() throws EnterpriseContextException;

    public void insert(EnterpriseObject in_enterpriseObject) throws EnterpriseContextException;

    public void update(EnterpriseObject in_enterpriseObject) throws EnterpriseContextException;

    public void delete(EnterpriseObject in_enterpriseObject) throws EnterpriseContextException;

    public void saveChanges() throws EnterpriseContextException;

    public void invalidateAllObjects() throws EnterpriseContextException;
}