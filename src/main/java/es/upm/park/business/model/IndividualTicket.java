package es.upm.park.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user. The ticket knows its user; the user does not
 * know its tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user) {
        this(id, date, basePrice, BigDecimal.ZERO, user);
    }

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, BigDecimal discount, User user) {
        super(id, date, basePrice, discount);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<User> holders() {
        return List.of(user);
    }

    @Override
    public boolean involves(User other) {
        return user.getDni().equals(other.getDni());
    }
}
