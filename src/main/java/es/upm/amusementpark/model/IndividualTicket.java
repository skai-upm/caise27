package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user. The ticket knows its user; users do not know their tickets.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal price;
    private final boolean partOfGroupTicket;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        this(id, date, user, price, false);
    }

    protected IndividualTicket(String id, LocalDate date, User user, BigDecimal price, boolean partOfGroupTicket) {
        super(id, date);
        this.user = user;
        this.price = price;
        this.partOfGroupTicket = partOfGroupTicket;
    }

    public User getUser() {
        return user;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<IndividualTicket> individualTickets() {
        return List.of(this);
    }

    /**
     * Tells whether this ticket belongs to the composition of a group ticket.
     */
    public boolean isPartOfGroupTicket() {
        return partOfGroupTicket;
    }
}
