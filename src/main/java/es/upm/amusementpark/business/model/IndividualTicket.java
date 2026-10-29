package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * A ticket for a single user. The ticket knows its user; the user does not
 * know its tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;

    public IndividualTicket(String identifier, User user, LocalDate date, BigDecimal price) {
        super(identifier, date, price);
        this.user = Objects.requireNonNull(user);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public boolean involves(User other) {
        return user.equals(other);
    }

    @Override
    public List<User> admittedUsers() {
        return List.of(user);
    }
}
