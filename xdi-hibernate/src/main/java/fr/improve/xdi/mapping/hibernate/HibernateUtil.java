package fr.improve.xdi.mapping.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * Basic Hibernate helper class, handles SessionFactory, Session and
 * Transaction.
 * <p>
 * Uses a static initializer for the initial SessionFactory creation and holds
 * Session and Transactions in thread local variables. All exceptions are
 * wrapped in an unchecked InfrastructureException.
 * 
 * @author christian@hibernate.org
 */
public class HibernateUtil {

	private static Log log = LogFactory.getLog(HibernateUtil.class);

	private static Configuration configuration;

	private static SessionFactory sessionFactory;

	private static final ThreadLocal threadSession = new ThreadLocal();

	private static final ThreadLocal threadTransaction = new ThreadLocal();

	private static final ThreadLocal threadInterceptor = new ThreadLocal();

	// Create the initial SessionFactory from the default configuration files
	/*static {
		try {
			configuration = new Configuration();
			sessionFactory = configuration.configure().buildSessionFactory();
			// We could also let Hibernate bind it to JNDI:
			// configuration.configure().buildSessionFactory()
		} catch (Exception ex) {
			// We have to catch Throwable, otherwise we will miss
			// NoClassDefFoundError and other subclasses of Error
			log.error("Building SessionFactory failed.", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}*/
	
	public static void setSessionFactory(SessionFactory in_sessionFactory) {
		sessionFactory = in_sessionFactory;
	}

	/**
	 * Returns the SessionFactory used for this static class.
	 * 
	 * @return SessionFactory
	 */
	public static SessionFactory getSessionFactory() {
		/*
		 * Instead of a static variable, use JNDI: SessionFactory sessions =
		 * null; try { Context ctx = new InitialContext(); String jndiName =
		 * "java:hibernate/HibernateFactory"; sessions =
		 * (SessionFactory)ctx.lookup(jndiName); } catch (NamingException ex) {
		 * throw new InfrastructureException(ex); } return sessions;
		 */
		return sessionFactory;
	}

	/**
	 * Returns the original Hibernate configuration.
	 * 
	 * @return Configuration
	 */
	public static Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Rebuild the SessionFactory with the static Configuration.
	 * 
	 */
	public static void rebuildSessionFactory() {
		synchronized (sessionFactory) {
			sessionFactory = getConfiguration().buildSessionFactory();
		}
	}

	/**
	 * Rebuild the SessionFactory with the given Hibernate Configuration.
	 * 
	 * @param cfg
	 */
	public static void rebuildSessionFactory(Configuration cfg) {
		synchronized (sessionFactory) {
			sessionFactory = cfg.buildSessionFactory();
			configuration = cfg;
		}
	}

	/**
	 * Retrieves the current Session local to the thread. <p/> If no Session is
	 * open, opens a new Session for the running thread.
	 * 
	 * @return Session
	 */
	public static Session getSession() {
		Session s = (Session) threadSession.get();
		if (s == null) {
			log.debug("Opening new Session for this thread.");
			if (getInterceptor() != null) {
				log.debug("Using interceptor: " + getInterceptor().getClass());
				s = getSessionFactory().openSession(getInterceptor());
			} else {
				s = getSessionFactory().openSession();
			}
			threadSession.set(s);
		}
		return s;
	}

	/**
	 * Closes the Session local to the thread.
	 */
	public static void closeSession() {
		Session s = (Session) threadSession.get();
		threadSession.set(null);
		if (s != null && s.isOpen()) {
			log.debug("Closing Session of this thread.");
			s.close();
		}
	}

	/**
	 * Start a new database transaction.
	 */
	public static void beginTransaction() {
		Transaction tx = (Transaction) threadTransaction.get();
		if (tx == null) {
			log.debug("Starting new database transaction in this thread.");
			tx = getSession().beginTransaction();
			threadTransaction.set(tx);
		}
	}

	/**
	 * Commit the database transaction.
	 */
	public static void commitTransaction() {
		Transaction tx = (Transaction) threadTransaction.get();
		try {
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
				log.debug("Committing database transaction of this thread.");
				tx.commit();
			}
			threadTransaction.set(null);
		} catch (HibernateException e) {
			rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Rollback the database transaction.
	 */
	public static void rollbackTransaction() {
		Transaction tx = (Transaction) threadTransaction.get();
		try {
			threadTransaction.set(null);
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
				log.debug("Tyring to rollback database transaction of this thread.");
				tx.rollback();
			}
		} finally {
			closeSession();
		}
	}

	/**
	 * Reconnects a Hibernate Session to the current Thread.
	 * 
	 * @param session
	 *            The Hibernate Session to be reconnected.
	 */
	public static void reconnect(Session session) {
		session.reconnect();
		threadSession.set(session);
	}

	/**
	 * Disconnect and return Session from current Thread.
	 * 
	 * @return Session the disconnected Session
	 */
	public static Session disconnectSession() {
		Session session = getSession();
		threadSession.set(null);
		if (session.isConnected() && session.isOpen())
			session.disconnect();
		return session;
	}

	/**
	 * Register a Hibernate interceptor with the current thread.
	 * <p>
	 * Every Session opened is opened with this interceptor after registration.
	 * Has no effect if the current Session of the thread is already open,
	 * effective on next close()/getSession().
	 */
	public static void registerInterceptor(Interceptor interceptor) {
		threadInterceptor.set(interceptor);
	}

	private static Interceptor getInterceptor() {
		Interceptor interceptor = (Interceptor) threadInterceptor.get();
		return interceptor;
	}

}
