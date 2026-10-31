package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** A ticket for a single user. */
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
    public List<User> getHolders() {
        return List.of(user);
    }
}
