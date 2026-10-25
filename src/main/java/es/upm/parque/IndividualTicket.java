package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An individual ticket for one date, held by a registered user.
 */
public class IndividualTicket extends Ticket {

    private final User holder;
    private final BigDecimal price;

    public IndividualTicket(String id, LocalDate date, User holder, BigDecimal price) {
        super(id, date);
        this.holder = holder;
        this.price = price;
    }

    public User getHolder() {
        return holder;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<User> getVisitors() {
        return List.of(holder);
    }
}
