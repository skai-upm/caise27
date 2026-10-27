package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket bought for a single user. The ticket knows its user; the user does
 * not know its tickets.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal price;
    private GroupTicket groupTicket;

    public IndividualTicket(String identifier, LocalDate date, User user, BigDecimal price) {
        super(identifier, date);
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

    /** Whether this ticket is part of the composition of a group ticket. */
    public boolean isPartOfGroupTicket() {
        return groupTicket != null;
    }

    /** The group ticket this ticket belongs to, or {@code null}. */
    public GroupTicket getGroupTicket() {
        return groupTicket;
    }

    void assignTo(GroupTicket groupTicket) {
        this.groupTicket = groupTicket;
    }
}
