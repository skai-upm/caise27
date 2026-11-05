package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket purchased for a single user. The ticket knows its user; the user does not know its tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

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

    @Override
    public List<User> admittedUsers() {
        return List.of(user);
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }
}
