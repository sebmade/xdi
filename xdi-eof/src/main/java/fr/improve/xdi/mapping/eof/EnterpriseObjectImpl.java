package fr.improve.xdi.mapping.eof;

import java.util.Set;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.exception.InvocationTargetKeyException;
import fr.improve.xdi.mapping.exception.UnknownKeyException;

/**
 * EnterpriseObject implementation for EOF
 *
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EnterpriseObjectImpl implements EnterpriseObject {
    private EOEnterpriseObject _object;

    public EnterpriseObjectImpl(EOEnterpriseObject in_object) {
        _object = in_object;
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#attributes()
    */
    @Override
    public Set attributes() {
        return new NSArraySet(_object.classDescription().attributeKeys());
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#keys()
    */
    @Override
    public Set keys() {
        return new NSArraySet(_object.classDescription().attributeKeys());
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#name()
    */
    @Override
    public String name() {
        return _object.entityName();
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#takeValueForKey(java.lang.Object, java.lang.String)
    */
    @Override
    public void takeValueForKey(Object in_value, String in_key)
        throws InvocationTargetKeyException, UnknownKeyException {
        try {
            if (in_value instanceof EnterpriseObject) {
                _object.takeValueForKey(((EnterpriseObject) in_value).baseObject(), in_key);
            } else if (in_value instanceof byte[]) {
                _object.takeValueForKey(new NSData((byte[])in_value), in_key);
            } else {
                _object.takeValueForKey(in_value, in_key);
            }
        } catch (NSKeyValueCoding.UnknownKeyException e) {
            try {
                if (in_value instanceof EnterpriseObject) {
                    _object.takeValueForKeyPath(((EnterpriseObject) in_value).baseObject(), in_key);
                } else {
                    _object.takeValueForKeyPath(in_value, in_key);
                }
            } catch (NSKeyValueCoding.UnknownKeyException ex) {
                throw new UnknownKeyException(e);
            } catch (Exception ex) {
                throw new InvocationTargetKeyException(e);
            }
        } catch (Exception e) {
            throw new InvocationTargetKeyException(e);
        }
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#toManyKeys()
    */
    @Override
    public Set toManyKeys() {
        return new NSArraySet(_object.classDescription().toManyRelationshipKeys());
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#toOneKeys()
    */
    @Override
    public Set toOneKeys() {
        return new NSArraySet(_object.classDescription().toOneRelationshipKeys());
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#valueForKey(java.lang.String)
    */
    @Override
    public Object valueForKey(String in_key) throws InvocationTargetKeyException, UnknownKeyException {
        Object l_obj = null;

        try {
            if (_object.editingContext() != null) {
                l_obj = _object.storedValueForKey(in_key);
            } else {
                l_obj = _object.valueForKey(in_key);
            }
        } catch (NSKeyValueCoding.UnknownKeyException e) {
            try {
                l_obj = _object.valueForKey(in_key);
                // Bizarrement lorsque l'on passe le nom d'une clé primaire invisible dans
                // l'eomodel dans un valueForKey, il n'y a pas d'exception UnknownKeyException
                // et la valeur retournée est null.
                // La modification consiste à tester dans le cas ou ça retourne null si la clé
                // passée correspond à une clé primaire en utilisant la méthode
                // primaryKeyForObject. Si cette clé ne correspond pas l_obj restera à null.
                if (l_obj == null) {
                    NSDictionary l_dict = EOUtilities.primaryKeyForObject(_object.editingContext(), _object);

                    if (l_dict != null)
                        l_obj = l_dict.objectForKey(in_key);
                }
            } catch (NSKeyValueCoding.UnknownKeyException e2) {
                NSDictionary l_dict = EOUtilities.primaryKeyForObject(_object.editingContext(), _object);

                if (l_dict != null)
                    l_obj = l_dict.objectForKey(in_key);

                if (l_obj == null) {
                    try {
                        l_obj = _object.valueForKeyPath(in_key);
                    } catch (NSKeyValueCoding.UnknownKeyException e3) {
                        throw new UnknownKeyException(e3);
                    } catch (Exception e3) {
                        throw new InvocationTargetKeyException(e3);
                    }
                }
            } catch (Exception e2) {
                throw new InvocationTargetKeyException(e2);
            }
        } catch (Exception e) {
            throw new InvocationTargetKeyException(e);
        }

        if (l_obj != null) {
            if (l_obj instanceof EOEnterpriseObject) {
                return new EnterpriseObjectImpl((EOEnterpriseObject) l_obj);
            }

            if (l_obj instanceof NSArray) {
                return new NSArraySet((NSArray) l_obj);
            }

            if (l_obj instanceof NSData) {
                return ((NSData) l_obj).bytes();
            }
        }

        return l_obj;
    }

    @Override
    public Object baseObject() {
        return _object;
    }

    @Override
    public boolean equals(Object in_obj) {
        if (in_obj instanceof EnterpriseObject) {
            return ((EnterpriseObject) in_obj).baseObject().equals(_object);
        } else {
            return false;
        }
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#addObjectToBothSidesOfRelationshipWithKey(fr.improve.edi.mapping.EnterpriseObject, java.lang.String)
    */
    @Override
    public void addObjectToBothSidesOfRelationshipWithKey(EnterpriseObject in_enterpriseObject, String in_key) throws InvocationTargetKeyException, UnknownKeyException {
        _object.addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) in_enterpriseObject.baseObject(), in_key);
    }

    /**
    * @see fr.improve.edi.mapping.EnterpriseObject#removeObjectFromBothSidesOfRelationshipWithKey(fr.improve.edi.mapping.EnterpriseObject, java.lang.String)
    */
    @Override
    public void removeObjectFromBothSidesOfRelationshipWithKey(EnterpriseObject in_enterpriseObject, String in_key) throws InvocationTargetKeyException, UnknownKeyException {
        _object.removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject) in_enterpriseObject.baseObject(), in_key);
    }

    @Override
    public Set toManyAttributes() {
        return null;
    }

    @Override
    public Set toOneAttributes() {
        return null;
    }
}
