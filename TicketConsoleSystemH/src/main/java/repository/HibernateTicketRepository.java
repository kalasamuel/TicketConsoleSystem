package repository;

import models.Ticket;
import org.hibernate.Session;
import org.hibernate.Transaction;
import storage.HibernateUtil;

import java.util.List;
import java.util.Optional;

/**
 * Session-per-operation implementation: each method opens its own Session, runs one
 * transaction, and closes cleanly. That's the right pattern for a CLI app that isn't
 * running inside a web request/response cycle with its own transaction boundary.
 */
public class HibernateTicketRepository implements TicketRepository {


    public Ticket save(Ticket ticket) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(ticket);
            session.flush(); 
            ticket.assignTicketCode();
            session.flush();
            tx.commit();
            return ticket;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }


    public Ticket update(Ticket ticket) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Ticket merged = session.merge(ticket);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }


    public void delete(Ticket ticket) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Ticket managed = session.get(Ticket.class, ticket.getId());
            if (managed != null) {
                session.remove(managed);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }


    public Optional<Ticket> findByCode(String ticketCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Ticket t = session.createQuery(
                    "from Ticket where ticketCode = :code", Ticket.class)
                    .setParameter("code", ticketCode)
                    .uniqueResult();
            return Optional.ofNullable(t);
        }
    }


    public List<Ticket> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Ticket order by id", Ticket.class).list();
        }
    }


    public List<Ticket> findByCustomerNameContaining(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Ticket where lower(customerName) like :name", Ticket.class)
                    .setParameter("name", "%" + name.toLowerCase() + "%")
                    .list();
        }
    }


    public List<Ticket> findByCategory(String category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Ticket where category = :category", Ticket.class)
                    .setParameter("category", category)
                    .list();
        }
    }


    public List<Ticket> findByStatus(Ticket.Status status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Ticket where status = :status", Ticket.class)
                    .setParameter("status", status)
                    .list();
        }
    }


    public List<Ticket> findByPriority(Ticket.Priority priority) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Ticket where priority = :priority", Ticket.class)
                    .setParameter("priority", priority)
                    .list();
        }
    }
}
