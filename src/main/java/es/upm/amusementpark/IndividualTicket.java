package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for one user, bought on its own or as part of a group ticket.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal price;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        super(id, date);
        this.user = user;
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }
}
