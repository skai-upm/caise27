package es.upm.etsisi.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user. The ticket knows its user; users do not know their tickets.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal price;

    public IndividualTicket(String identifier, LocalDate date, User user, BigDecimal price) {
        super(identifier, date);
        this.user = user;
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    /** Price charged for this single ticket. */
    public BigDecimal getUnitPrice() {
        return price;
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return List.of(this);
    }
}
