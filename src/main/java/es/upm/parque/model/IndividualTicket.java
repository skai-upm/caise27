package es.upm.parque.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ticket for a single user. The ticket knows its user; the user does not know its tickets.
 */
public class IndividualTicket extends Ticket {

    private final User user;

    public IndividualTicket(String id, User user, LocalDate date, BigDecimal price) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean belongsTo(User other) {
        return user.getDni().equals(other.getDni());
    }
}
