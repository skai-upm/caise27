package es.upm.etsisi.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for a single user, either bought on its own or as part of a group ticket.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, int discountPercent) {
        super(id, date);
        this.user = user;
        this.basePrice = Money.of(basePrice);
        this.discountPercent = discountPercent;
    }

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
    public List<User> getHolders() {
        return List.of(user);
    }
}
