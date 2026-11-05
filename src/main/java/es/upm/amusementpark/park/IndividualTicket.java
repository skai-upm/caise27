package es.upm.amusementpark.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Ticket for a single user. The ticket knows its user (by e-mail); users never hold their tickets.
 */
public class IndividualTicket extends Ticket {

    private final String userEmail;
    private final BigDecimal price;
    private GroupTicket containingGroupTicket;

    public IndividualTicket(String id, String userEmail, LocalDate date, BigDecimal price) {
        super(id, date);
        this.userEmail = Objects.requireNonNull(userEmail, "userEmail");
        this.price = Objects.requireNonNull(price, "price");
    }

    public String getUserEmail() {
        return userEmail;
    }

    /** @return the group ticket this ticket belongs to, or {@code null} for a standalone ticket */
    public GroupTicket getContainingGroupTicket() {
        return containingGroupTicket;
    }

    void setContainingGroupTicket(GroupTicket groupTicket) {
        containingGroupTicket = groupTicket;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<String> holders() {
        return List.of(userEmail);
    }

    @Override
    public boolean concernsUser(String email) {
        return userEmail.equals(email);
    }
}
