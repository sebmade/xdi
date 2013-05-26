package fr.improve.xdi.mapping.hibernate;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 * NOT USED
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class AuditInterceptor implements Interceptor, Serializable {
    private int updates;
    private int creates;

    public AuditInterceptor() {
    }

    public void onDelete(Object obj, Serializable serializable, Object[] aobj, String[] as, Type[] atype) {
    }

    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] aobj, String[] as, Type[] atype) {
        return true;
    }

    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] as, Type[] atype) {
        return false;
    }

    public boolean onSave(Object entity, Serializable id, Object[] state, String[] as, Type[] atype) {
        return true;
    }

    public void postFlush(Iterator entities) {
        System.out.println("Creations: " + creates + ", Updates: " + updates);
    }

    public void preFlush(Iterator entities) {
        updates = 0;
        creates = 0;
    }

    public int[] findDirty(Object arg0, Serializable arg1, Object[] arg2, Object[] aobj, String[] as, Type[] atype) {
        return null;
    }

    public Object instantiate(Class arg0, Serializable arg1)
        throws CallbackException {
        return null;
    }

    public Boolean isUnsaved(Object arg0) {
        return null;
    }

	public Boolean isTransient(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object instantiate(String entityName, EntityMode entityMode, Serializable id) throws CallbackException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityName(Object object) throws CallbackException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getEntity(String entityName, Serializable id) throws CallbackException {
		// TODO Auto-generated method stub
		return null;
	}

	public void afterTransactionBegin(Transaction tx) {
		// TODO Auto-generated method stub
		
	}

	public void beforeTransactionCompletion(Transaction tx) {
		// TODO Auto-generated method stub
		
	}

	public void afterTransactionCompletion(Transaction tx) {
		// TODO Auto-generated method stub
		
	}

    public void onCollectionRecreate(Object arg0, Serializable arg1) throws CallbackException {
        // TODO Auto-generated method stub
        
    }

    public void onCollectionRemove(Object arg0, Serializable arg1) throws CallbackException {
        // TODO Auto-generated method stub
        
    }

    public void onCollectionUpdate(Object arg0, Serializable arg1) throws CallbackException {
        // TODO Auto-generated method stub
        
    }

    public String onPrepareStatement(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
