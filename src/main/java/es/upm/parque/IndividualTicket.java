package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for a single user. It may belong to a group ticket that generated it.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal price;
    private GroupTicket groupTicket;

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
    public List<User> getUsers() {
        return List.of(user);
    }

    public GroupTicket getGroupTicket() {
        return groupTicket;
    }

    void setGroupTicket(GroupTicket groupTicket) {
        this.groupTicket = groupTicket;
    }
}
