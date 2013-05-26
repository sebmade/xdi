package fr.improve.xdi.mapping.hibernate;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import fr.improve.xdi.mapping.AbstractEnterpriseContext;
import fr.improve.xdi.mapping.EnterpriseObject;
import fr.improve.xdi.mapping.SortOrdering;
import fr.improve.xdi.mapping.exception.EnterpriseContextException;

/**
 * EnterpriseContext implementation for Hibernate
 * Equivalent object : SessionFactory
 * 
 * NOTE :
 * 		Actually you must extends hibernate database objects with EnterpriseContextImpl
 * 		It will change in future version
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EnterpriseContextImpl extends AbstractEnterpriseContext {
    static Log log = LogFactory.getLog(EnterpriseContextImpl.class.getName());

    //private static SessionFactory _sessionFactory = null;

    public static String MAPPING_PACKAGE = null;

    public static String ENTERPRISE_PACKAGE = null;

    //private Session _session = null;

    //private Transaction _transaction = null;

    //private int transactionCount = 0;

    public EnterpriseContextImpl() throws EnterpriseContextException {
        if (HibernateUtil.getSessionFactory() != null) {
            /*try {
                _session = _sessionFactory.openSession();
            } catch (HibernateException e) {
                throw new EnterpriseContextException(e);
            }*/
        } else {
            throw new EnterpriseContextException("Hibernate Enterprise Context not initialized, use configure(URL) method !");
        }
    }
    
    public EnterpriseContextImpl(URL in_url) throws EnterpriseContextException {
        if (HibernateUtil.getSessionFactory() == null) {
            configure(in_url);
        }
    }
    
    public static void setEnterprisePackage(String in_package) {
        ENTERPRISE_PACKAGE = in_package;
    }

    public static void setMappingPackage(String in_package) {
        MAPPING_PACKAGE = in_package;
    }

    public static void configure(URL in_url) throws EnterpriseContextException {
        try {
            Configuration l_config = (new Configuration()).configure(in_url);
            setEnterprisePackage(l_config.getProperty("package.enterprise"));
            setMappingPackage(l_config.getProperty("package.mapping"));
            //_sessionFactory = l_config.buildSessionFactory();
            HibernateUtil.setSessionFactory(l_config.buildSessionFactory());
        } catch (Exception e) {
            throw new EnterpriseContextException(e);
        }
    }

    /*public SessionFactory getSessionFactory() {
        return _sessionFactory;
    }*/

    /*public Session getSession() throws HibernateException {
        if (_session == null || !_session.isOpen()) {
            _session = _sessionFactory.openSession();
            log.debug("****** INTEGRATOR SESSION CREATED");
        } else if (!_session.isConnected()) {
            _session.reconnect();
            log.debug("****** INTEGRATOR SESSION RECONNECTED");
        }
        return _session;
    }*/

    public EnterpriseObject createInstance(String in_className) throws EnterpriseContextException {
        try {
            EnterpriseObjectImpl l_object = (EnterpriseObjectImpl)getClassWithName(in_className).newInstance();
            return l_object;
        } catch (Exception e) {
            throw new EnterpriseContextException(e);
        }
    }

    public EnterpriseObject createAndInsertInstance(String in_className) throws EnterpriseContextException {
        EnterpriseObject l_object = createInstance(in_className);
        insert(l_object);
        return l_object;
    }
    
    public EnterpriseObject objectWithID(String in_name, Object in_primaryKey) throws EnterpriseContextException {
        try {
        	HibernateUtil.beginTransaction();
        	EnterpriseObject l_result = (EnterpriseObject)HibernateUtil.getSession().load(getClassWithName(in_name), (Serializable)in_primaryKey);
            HibernateUtil.commitTransaction();
            return l_result;
        } catch (ObjectNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new EnterpriseContextException(e);
        }
    }

    public Collection objectsWithClassNamed(String in_name) throws EnterpriseContextException {
        return objectsWithClassNamedBindingValues(in_name, null, null);
    }

    /*
     * @see fr.improve.xdi.mapping.EnterpriseContext#objectsWithClassNamed(java.lang.String,
     *      java.util.Set)
     */
    public Collection objectsWithClassNamed(String in_name, Set in_orders) throws EnterpriseContextException {
        return objectsWithClassNamedBindingValues(in_name, null, in_orders);
    }

    public Collection objectsWithClassNamedBindingValue(String in_name, String in_key, Object in_value) throws EnterpriseContextException {
        try {
        	HibernateUtil.getSession().setFlushMode(FlushMode.NEVER);
        	//HibernateUtil.beginTransaction();
            List l_result = HibernateUtil.getSession().createCriteria(getClassWithName(in_name)).add(Expression.eq(in_key, in_value)).list();
            //HibernateUtil.commitTransaction();
        	HibernateUtil.getSession().setFlushMode(FlushMode.AUTO);
            return l_result;
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        } catch (ClassNotFoundException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public Collection objectsWithClassNamedBindingValues(String in_name, Map in_values) throws EnterpriseContextException {
        return objectsWithClassNamedBindingValues(in_name, in_values, null);
    }

    public Collection objectsWithClassNamedBindingValues(String in_name, Map in_values, Set in_orders) throws EnterpriseContextException {
        try {
        	HibernateUtil.getSession().setFlushMode(FlushMode.NEVER);
        	//HibernateUtil.beginTransaction();
            Criteria l_criteria = HibernateUtil.getSession().createCriteria(getClassWithName(in_name));

            if (in_values != null) {
                for (Iterator l_keys = in_values.keySet().iterator(); l_keys.hasNext();) {
                    String l_key = (String) l_keys.next();
                    Object l_value = in_values.get(l_key);

                    if (l_value != null) {
                        l_criteria.add(Expression.eq(l_key, l_value));
                    } else {
                        l_criteria.add(Expression.isNull(l_key));
                    }
                }
            }

            if (in_orders != null) {
                for (Iterator l_keys = in_orders.iterator(); l_keys.hasNext();) {
                    l_criteria.addOrder((Order) ((SortOrdering) l_keys.next()).getOrder());
                }
            }

            List l_list = l_criteria.list();
            //HibernateUtil.commitTransaction();
        	HibernateUtil.getSession().setFlushMode(FlushMode.AUTO);
            return l_list;
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        } catch (ClassNotFoundException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public Collection objectsWithClassNamedBindingLikeValues(String in_name, Map in_values) throws EnterpriseContextException {
        try {
        	HibernateUtil.getSession().setFlushMode(FlushMode.NEVER);
        	//HibernateUtil.beginTransaction();
            Criteria l_criteria = HibernateUtil.getSession().createCriteria(getClassWithName(in_name));

            if (in_values != null) {
                for (Iterator l_keys = in_values.keySet().iterator(); l_keys.hasNext();) {
                    String l_key = (String) l_keys.next();
                    Object l_value = in_values.get(l_key);

                    if (l_value != null) {
                        if (l_value instanceof String) {
                            if (((String) l_value).length() > 0) {
                                l_criteria.add(Expression.like(l_key, l_value + "%"));
                            }
                        } else {
                            l_criteria.add(Expression.eq(l_key, l_value));
                        }
                    } else {
                        l_criteria.add(Expression.isNull(l_key));
                    }
                }
            }

            List l_list = l_criteria.list();
            //HibernateUtil.commitTransaction();
        	HibernateUtil.getSession().setFlushMode(FlushMode.AUTO);
            return l_list;
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        } catch (ClassNotFoundException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public static Class getClassWithName(String in_className) throws ClassNotFoundException {
        Class l_class = null;

        try {
            l_class = Class.forName(ENTERPRISE_PACKAGE + "." + in_className);
        } catch (ClassNotFoundException e) {
            try {
                l_class = Class.forName(MAPPING_PACKAGE + "." + in_className);
            } catch (ClassNotFoundException e1) {
                throw e1;
            }
        }

        return l_class;
    }

    public void beginTransaction() throws EnterpriseContextException {
        try {
        	HibernateUtil.beginTransaction();
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public void endTransaction() throws EnterpriseContextException {
        /*try {
            transactionCount--;

            if (transactionCount == 0) {
                if (_session != null && _session.isOpen() && _session.isConnected()) {
                    if ((_transaction != null) && (!_transaction.wasCommitted() || !_transaction.wasRolledBack())) {
                        invalidateAllObjects();
                    }
                    
                    log.debug("****** INTEGRATOR SESSION DISCONNECT");
                    _session.disconnect();
                }
                _transaction = null;
            }
        } catch (HibernateException e) {
            //throw new EnterpriseContextException(e);
            log.warn(e);
        }*/
    }

    public void insert(EnterpriseObject in_object) throws EnterpriseContextException {
        try {
        	HibernateUtil.getSession().save(in_object.baseObject());
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public void update(EnterpriseObject in_object) throws EnterpriseContextException {
        try {
        	HibernateUtil.getSession().saveOrUpdate(in_object.baseObject());
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public void delete(EnterpriseObject in_object) throws EnterpriseContextException {
        try {
        	HibernateUtil.getSession().delete(in_object.baseObject());
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public void saveChanges() throws EnterpriseContextException {
        try {
            if (enterpriseContextHandler != null) {
                enterpriseContextHandler.willSaveChanges(this);
            }
            HibernateUtil.commitTransaction();
            if (enterpriseContextHandler != null) {
                enterpriseContextHandler.didSaveChanges(this);
            }
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        }
    }

    public void invalidateAllObjects() throws EnterpriseContextException {
        try {
        	HibernateUtil.rollbackTransaction();
        } catch (HibernateException e) {
            throw new EnterpriseContextException(e);
        }
    }

    /*public void finalize() {
        try {
        	HibernateUtil.closeSession();
        } catch (HibernateException e) {
            log.warn(e);
        }
    }*/

    /*
     * @see fr.improve.xdi.mapping.EnterpriseContext#sortOrderings(java.lang.String,
     *      int)
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

