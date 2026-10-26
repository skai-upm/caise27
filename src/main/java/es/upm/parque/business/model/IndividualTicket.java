package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An individual park ticket for one user and one date, sold at a fixed price.
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
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }
}
