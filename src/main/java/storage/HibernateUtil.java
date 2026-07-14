package storage;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Builds a single, shared SessionFactory from hibernate.cfg.xml on first use.
 * A SessionFactory is expensive to create and thread-safe to share, so the whole
 * application should go through this one instance rather than each class building its own.
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("[HibernateUtil] SessionFactory creation failed: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
