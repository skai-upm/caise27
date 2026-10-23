package es.upm.etsisi.park.model.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.park.user.User;

/** Ticket for a single user, priced as the base price minus a percentage discount. */
public class IndividualTicket extends Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, int discountPercent) {
        super(id, date);
        this.user = user;
        this.basePrice = Money.of(basePrice);
        this.discountPercent = discountPercent;
    }

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice) {
        this(id, date, user, basePrice, 0);
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
        return Money.of(basePrice.multiply(BigDecimal.valueOf(100L - discountPercent)).divide(HUNDRED));
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }
}
