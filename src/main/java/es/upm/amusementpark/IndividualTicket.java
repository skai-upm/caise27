package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A ticket bought for a single user. The ticket knows its user; the user does
 * not know its tickets.
 */
public class IndividualTicket extends Ticket {

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
    public BigDecimal getPrice() {
        return price;
    }

    /** Whether this ticket is part of the composition of a group ticket. */
    public boolean isPartOfGroupTicket() {
        return false;
    }
}
