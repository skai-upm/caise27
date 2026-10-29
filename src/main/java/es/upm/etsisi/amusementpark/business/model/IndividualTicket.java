package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for a single user. The ticket knows its user; the user does not know its tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, int discountPercent, User user) {
        super(id, date, basePrice, discountPercent);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public BigDecimal getPrice() {
        return unitPrice();
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public Ticket findTicket(String ticketId) {
        return getId().equals(ticketId) ? this : null;
    }

    @Override
    public List<IndividualTicket> entryTickets() {
        return List.of(this);
    }
}
