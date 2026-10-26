package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for one user on one date.
 */
public class IndividualTicket extends Ticket {

    private final User user;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }
}
