package repository;

import models.Ticket;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over however tickets are actually persisted.
 * TicketService depends only on this interface, not on Hibernate directly —
 * that's what makes a future swap (e.g. to a different ORM, or back to files
 * for a unit test) a one-class change instead of a rewrite.
 */
public interface TicketRepository {

    Ticket save(Ticket ticket);

    Ticket update(Ticket ticket);

    void delete(Ticket ticket);

    Optional<Ticket> findByCode(String ticketCode);

    List<Ticket> findAll();

    List<Ticket> findByCustomerNameContaining(String name);

    List<Ticket> findByCategory(String category);

    List<Ticket> findByStatus(Ticket.Status status);

    List<Ticket> findByPriority(Ticket.Priority priority);
}
