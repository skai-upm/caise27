package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ticket purchased for a single user. The ticket knows its user; the user does not know its tickets.
 */
public class IndividualTicket extends Ticket {

    private final User user;

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user) {
        super(id, date, basePrice);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean involves(User other) {
        return user.equals(other);
    }
}
