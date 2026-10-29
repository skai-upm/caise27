package es.upm.amusementpark.business.model.ticket;

import es.upm.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user, bought on its own or as part of a group ticket's composition.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, int discountPercent) {
        super(id, date);
        this.user = user;
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
    }

    @Override
    public User getUser() {
        return user;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public BigDecimal getPrice() {
        return applyDiscount(basePrice, discountPercent);
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> getAccessTickets() {
        return List.of(this);
    }
}
