package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for a single user. The user does not know their tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, int discountPercent) {
        super(id, date);
        this.user = user;
        this.basePrice = basePrice;
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
        return basePrice.multiply(BigDecimal.valueOf(100L - discountPercent))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> entryTickets() {
        return List.of(this);
    }

    @Override
    public IndividualTicket individualTicket() {
        return this;
    }
}
