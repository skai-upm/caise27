package es.upm.etsisi.amusementpark.business.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.amusementpark.business.user.User;

/**
 * A ticket for one user, either standalone or part of a group ticket's composition.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {
    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;

    public IndividualTicket(String identifier, LocalDate date, User user, BigDecimal basePrice,
                            int discountPercent) {
        super(identifier, date);
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
        return discounted(basePrice, discountPercent);
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> getEntryTickets() {
        return List.of(this);
    }
}
