package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket purchased for a single user. The ticket knows its user; the user does not know its tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user) {
        this(id, date, basePrice, BigDecimal.ZERO, user);
    }

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, BigDecimal discountRate, User user) {
        super(id, date, basePrice, discountRate);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean involves(User other) {
        return user.equals(other);
    }

    @Override
    public List<User> holders() {
        return List.of(user);
    }
}
