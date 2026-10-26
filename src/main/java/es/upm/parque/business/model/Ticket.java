package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction of every park ticket: an identifier, a date of use and a price.
 */
public abstract class Ticket implements Accessible {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    public abstract BigDecimal getPrice();

    /**
     * The users admitted with this ticket.
     */
    @Override
    public abstract List<User> getHolders();

    /**
     * The individual tickets this ticket stands for: itself, or the tickets composing a group ticket.
     */
    public abstract List<IndividualTicket> getIndividualTickets();

    /**
     * True if the ticket is for {@code today} or a later day.
     */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * True if the ticket is for a day strictly after {@code today}.
     */
    public boolean isFuture(LocalDate today) {
        return date.isAfter(today);
    }
}
