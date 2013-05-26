package fr.improve.xdi.mapping;

import java.io.Serializable;
import java.util.Set;

import fr.improve.xdi.mapping.exception.InvocationTargetKeyException;
import fr.improve.xdi.mapping.exception.UnknownKeyException;

/**
 * Interface implemented for each mapping tool
 * Used to define a database object
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface EnterpriseObject extends Serializable {
    public Object valueForKey(String s) throws InvocationTargetKeyException, UnknownKeyException;

    public void takeValueForKey(Object obj, String s) throws InvocationTargetKeyException, UnknownKeyException;

    public String name();

    public Set keys();

    public Set toOneKeys();

    public Set toManyKeys();

    public Set attributes();

    public Set toOneAttributes();

    public Set toManyAttributes();

    public Object baseObject();

    public void addObjectToBothSidesOfRelationshipWithKey(EnterpriseObject enterpriseobject, String s) throws InvocationTargetKeyException, UnknownKeyException;

    public void removeObjectFromBothSidesOfRelationshipWithKey(EnterpriseObject enterpriseobject, String s) throws InvocationTargetKeyException, UnknownKeyException;
}
