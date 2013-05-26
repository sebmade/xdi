package fr.improve.xdi.mapping.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import fr.improve.xdi.mapping.Attribute;
import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.StringUtils;
import fr.improve.xdi.mapping.exception.InvocationTargetKeyException;
import fr.improve.xdi.mapping.exception.UnknownKeyException;
import fr.improve.xdi.resources.Messages;

/**
 * EnterpriseObject implementation for Hibernate
 *
 * NOTE :
 * 		Actually you must extends hibernate database objects with EnterpriseContextImpl
 * 		It will change in future version
 *
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EnterpriseObjectImpl implements EnterpriseObject, Comparable {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnterpriseObjectImpl.class);
    private Set _attributes = null;
    private Set _toOneAttributes = null;
    private Set _toManyAttributes = null;
    private Set _keys = null;
    private Set _toOneKeys = null;
    private Set _toManyKeys = null;
    private HashMap __keys = null;
    private HashMap __toOneKeys = null;
    private HashMap __toManyKeys = null;
    //private Set _fields = null;
    //private EnterpriseContext _enterpriseContext;

    /*public void setEnterpriseContext(EnterpriseContext in_enterpriseContext) {
        _enterpriseContext = in_enterpriseContext;
    }*/

    public Object baseObject() {
        return this;
    }

    public Set attributes() {
        if (_attributes == null) {
            _attributes = new HashSet();

            /*Field[] l_fields = _getMappingClass(getClass()).getDeclaredFields();

            for (int i = 0; i < l_fields.length; i++) {
                _attributes.add(new Attribute(l_fields[i].getName(), l_fields[i].getType()));
            }*/
            Iterator l_keys = _keys().keySet().iterator();
            while (l_keys.hasNext()) {
                String l_key = (String)l_keys.next();
                _attributes.add(new Attribute(l_key, (Class)_keys().get(l_key)));
            }
        }

        return _attributes;
    }

    public Set toOneAttributes() {
        if (_toOneAttributes == null) {
            _toOneAttributes = new HashSet();

            /*Field[] l_fields = _getMappingClass(getClass()).getDeclaredFields();

            for (int i = 0; i < l_fields.length; i++) {
                if (_isMappingClass(l_fields[i].getType())) {
                    _toOneAttributes.add(new Attribute(l_fields[i].getName(), l_fields[i].getType()));
                }
            }*/
            Iterator l_keys = _toOneKeys().keySet().iterator();
            while (l_keys.hasNext()) {
                String l_key = (String)l_keys.next();
                _toOneAttributes.add(new Attribute(l_key, (Class)_toOneKeys().get(l_key)));
            }
        }

        return _toOneAttributes;
    }

    public Set toManyAttributes() {
        if (_toManyAttributes == null) {
            _toManyAttributes = new HashSet();

            /*Field[] l_fields = _getMappingClass(getClass()).getDeclaredFields();

            for (int i = 0; i < l_fields.length; i++) {
                if (java.util.Collection.class.isAssignableFrom(l_fields[i].getType())) {
                    _toManyAttributes.add(new Attribute(l_fields[i].getName(), l_fields[i].getType()));
                }
            }*/
            Iterator l_keys = _toManyKeys().keySet().iterator();
            while (l_keys.hasNext()) {
                String l_key = (String)l_keys.next();
                _toManyAttributes.add(new Attribute(l_key, (Class)_toManyKeys().get(l_key)));
            }
        }

        return _toManyAttributes;
    }

    private ClassMetadata _metaData = null;

    private ClassMetadata _getMetaData() {
        if (_metaData == null) {
            try {
                SessionFactory l_sessionFactory = HibernateUtil.getSessionFactory();
                _metaData = l_sessionFactory.getClassMetadata(getClass());
            } catch (Exception e) {
                log.warn("Exception occurred", e);
            }
        }

        return _metaData;
    }

    public Set keys() {
        if (_keys == null) {
            _keys = new HashSet();

            try {
                Map l_map = PropertyUtils.describe(this);
                Iterator l_keys = l_map.keySet().iterator();
                while (l_keys.hasNext()) {
                    String l_key = (String)l_keys.next();
                    Object l_value = l_map.get(l_key);
                    if (l_value != null
                        && !Set.class.isAssignableFrom(l_value.getClass())
                        && !_isMappingClass(l_value.getClass())) {
                        _keys.add(l_key);
                    }
                }
            } catch (Exception e) {
                log.warn("Exception occurred", e);
            }


            /*String[] l_keys = _getMetaData().getPropertyNames();
            Type[] l_types = _getMetaData().getPropertyTypes();

            for (int i = 0; i < l_keys.length; i++) {
                if (!l_types[i].isEntityType() && !l_types[i].isPersistentCollectionType()) {
                    _keys.add(l_keys[i]);
                }
            }*/

            /*Field[] l_fields = _getMappingClass(getClass()).getDeclaredFields();
            for (int i = 0; i < l_fields.length; i++) {
                if (
                    !java.util.Collection.class.isAssignableFrom(l_fields[i].getType())
                        && !_isMappingClass(l_fields[i].getType())) {
                    _keys.add(l_fields[i].getName());
                }
            }*/
        }

        return _keys;
    }

    public Map _keys() {
        if (__keys == null) {
            __keys = new HashMap();

            String[] l_keys = _getMetaData().getPropertyNames();
            Type[] l_types = _getMetaData().getPropertyTypes();

            for (int i = 0; i < l_keys.length; i++) {
                if (!l_types[i].isEntityType() && !l_types[i].isCollectionType()) {
                    __keys.put(l_keys[i], l_types[i].getReturnedClass());
                }
            }
        }

        return __keys;
    }

    public Set toOneKeys() {
        if (_toOneKeys == null) {
            _toOneKeys = new HashSet();

            try {
                Map l_map = PropertyUtils.describe(this);
                Iterator l_keys = l_map.keySet().iterator();
                while (l_keys.hasNext()) {
                    String l_key = (String)l_keys.next();
                    Object l_value = l_map.get(l_key);
                    if (l_value != null
                        && _isMappingClass(l_value.getClass())) {
                        _toOneKeys.add(l_key);
                    }
                }
            } catch (Exception e) {
                log.warn("Exception occurred", e);
            }

            /*String[] l_keys = _getMetaData().getPropertyNames();
            Type[] l_types = _getMetaData().getPropertyTypes();

            for (int i = 0; i < l_keys.length; i++) {
                if (l_types[i].isEntityType()) {
                    _toOneKeys.add(l_keys[i]);
                }
            }*/

            /*Field[] l_fields = _getMappingClass(getClass()).getDeclaredFields();
            for (int i = 0; i < l_fields.length; i++) {
                if (_isMappingClass(l_fields[i].getType())) {
                    _toOneKeys.add(l_fields[i].getName());
                }
            }*/
        }

        return _toOneKeys;
    }

    public Map _toOneKeys() {
        if (__toOneKeys == null) {
            __toOneKeys = new HashMap();

            String[] l_keys = _getMetaData().getPropertyNames();
            Type[] l_types = _getMetaData().getPropertyTypes();

            for (int i = 0; i < l_keys.length; i++) {
                if (l_types[i].isEntityType()) {
                    __toOneKeys.put(l_keys[i], l_types[i].getReturnedClass());
                }
            }
        }

        return __toOneKeys;
    }

    public Set toManyKeys() {
        if (_toManyKeys == null) {
            _toManyKeys = new HashSet();

            try {
                Map l_map = PropertyUtils.describe(this);
                Iterator l_keys = l_map.keySet().iterator();
                while (l_keys.hasNext()) {
                    String l_key = (String)l_keys.next();
                    Object l_value = l_map.get(l_key);
                    if (l_value != null
                        && Set.class.isAssignableFrom(l_value.getClass())) {
                        _toManyKeys.add(l_key);
                    }
                }
            } catch (Exception e) {
                log.warn("Exception occurred", e);
            }

            /*String[] l_keys = _getMetaData().getPropertyNames();
            Type[] l_types = _getMetaData().getPropertyTypes();

            for (int i = 0; i < l_keys.length; i++) {
                if (l_types[i].isPersistentCollectionType()) {
                    _toManyKeys.add(l_keys[i]);
                }
            }*/

            /*Field[] l_fields = _getMappingClass(getClass()).getDeclaredFields();
            for (int i = 0; i < l_fields.length; i++) {
                if (java.util.Set.class.isAssignableFrom(l_fields[i].getType())) {
                    _toManyKeys.add(l_fields[i].getName());
                }
            }*/
        }

        return _toManyKeys;
    }

    public Map _toManyKeys() {
        if (_toManyKeys == null) {
            __toManyKeys = new HashMap();

            String[] l_keys = _getMetaData().getPropertyNames();
            Type[] l_types = _getMetaData().getPropertyTypes();

            for (int i = 0; i < l_keys.length; i++) {
                if (l_types[i].isCollectionType()) {
                    __toManyKeys.put(l_keys[i], l_types[i].getReturnedClass());
                }
            }
        }

        return __toManyKeys;
    }

    public String name() {
        String l_name = getClass().getName();

        return l_name.substring(l_name.lastIndexOf('.') + 1);
    }

    protected String keyName(EnterpriseObject in_object) {
        for (Iterator l_toOneAttributes = in_object.toOneAttributes().iterator(); l_toOneAttributes.hasNext();) {
            Attribute l_attr = (Attribute) l_toOneAttributes.next();

            if (l_attr.getType().isAssignableFrom(getClass())) {
                return l_attr.getKey();
            }
        }

        return null;
    }

    public void addObjectToBothSidesOfRelationshipWithKey(EnterpriseObject in_object, String in_key)
        throws InvocationTargetKeyException, UnknownKeyException {
        if (_toOneKeys().keySet().contains(in_key)) {
            takeValueForKey(in_object, in_key);
        } else if (_toManyKeys().keySet().contains(in_key)) {
            String l_keyName = keyName(in_object);
            if (l_keyName != null) {
                in_object.takeValueForKey(this, l_keyName);
            }

            // utilisation de la méthode addToXxxx pour ne pas avoir à gérer les types de tableaux utilisés
            /*Set l_array = (Set) valueForKey(in_key);

            if (l_array == null) {
                l_array = new java.util.HashSet();
                takeValueForKey(l_array, in_key);
            }

            l_array.add(in_object);*/
            try {
                getClass().getMethod("addTo"+StringUtils.capitalizedString(in_key), new Class[] {Object.class}).invoke(this, new Object[] {in_object});
            } catch (NoSuchMethodException e) {
                throw new UnknownKeyException(Messages.getMessage("relationshipException02", "addTo"+StringUtils.capitalizedString(in_key), name()));
            } catch (InvocationTargetException e) {
                throw new InvocationTargetKeyException(Messages.getMessage("relationshipException03", "addTo"+StringUtils.capitalizedString(in_key), name(), e.getTargetException().getMessage()));
            } catch (Exception e) {
                throw new InvocationTargetKeyException(Messages.getMessage("relationshipException03", "addTo"+StringUtils.capitalizedString(in_key), name(), e.getMessage()));
            }
        } else {
            throw new InvocationTargetKeyException(Messages.getMessage("relationshipException00", in_object.name(), name()));
        }
    }

    public void removeObjectFromBothSidesOfRelationshipWithKey(EnterpriseObject in_object, String in_key)
        throws InvocationTargetKeyException, UnknownKeyException {
        if (_toOneKeys().keySet().contains(in_key)) {
            takeValueForKey(null, in_key);
        } else if (_toManyKeys().keySet().contains(in_key)) {
            String l_keyName = keyName(in_object);
            if (l_keyName != null) {
                in_object.takeValueForKey(null, l_keyName);
            }

            Collection l_array = (Collection) valueForKey(in_key);

            if (l_array != null) {
                l_array.remove(in_object);
            }
        } else {
            throw new InvocationTargetKeyException(Messages.getMessage("relationshipException01", in_object.name(), name()));
        }
    }

    public void takeValueForKey(Object in_value, String in_key)
        throws InvocationTargetKeyException, UnknownKeyException {
        try {
            PropertyUtils.setProperty(this, in_key, in_value);
            //((EnterpriseContextImpl)_enterpriseContext).getSession().update(this);
        } catch (IllegalAccessException e) {
            throw new InvocationTargetKeyException(e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetKeyException(e);
        } catch (NoSuchMethodException e) {
            throw new UnknownKeyException(in_key);
        //} catch (HibernateException e) {
        //	throw new InvocationTargetKeyException(e);
        }
    }

    private boolean _isMappingClass(Class in_class) {
        if (in_class.isPrimitive() || in_class.isArray()) {
            return false;
        }

        return in_class.getPackage().getName().equals(EnterpriseContextImpl.MAPPING_PACKAGE)
        || in_class.getPackage().getName().equals(EnterpriseContextImpl.ENTERPRISE_PACKAGE);
    }

    /*private Class _getMappingClass(Class in_class) {
        if (in_class.isPrimitive() || in_class.isArray()) {
            return in_class;
        }

        if (in_class.getPackage().getName().equals(EnterpriseContextImpl.ENTERPRISE_PACKAGE)) {
            return in_class.getSuperclass();
        } else {
            return in_class;
        }
    }*/

    public Object valueForKey(String in_key) throws InvocationTargetKeyException, UnknownKeyException {
        Object l_result = null;

        try {
            l_result = PropertyUtils.getProperty(this, in_key);
        } catch (IllegalAccessException e) {
            throw new InvocationTargetKeyException(e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetKeyException(e);
        } catch (NoSuchMethodException e) {
            throw new UnknownKeyException(in_key);
        }

        return l_result;
    }

    public boolean equals(Object in_obj) {
        if (in_obj instanceof EnterpriseObject) {
            return ((EnterpriseObject) in_obj).baseObject().equals(this);
        } else {
            return false;
        }
    }

    protected boolean assertValue(Object in_value, String in_key)
        throws InvocationTargetKeyException, UnknownKeyException {
        Object l_currentValue = valueForKey(in_key);

        return (l_currentValue == null) || !l_currentValue.equals(in_value);
    }

    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object in_obj) {
        if (!equals(in_obj)) {
            return 1;
        }
        return 0;
    }
}
