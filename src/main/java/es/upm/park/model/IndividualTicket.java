package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A ticket for a single user. The ticket knows its user; the user does not
 * know its tickets.
 */
public class IndividualTicket extends Ticket {

    private final User user;

    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean involves(User other) {
        return user.getDni().equals(other.getDni());
    }
}
