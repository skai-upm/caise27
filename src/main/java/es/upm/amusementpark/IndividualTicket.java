package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user, bought alone or as part of a group ticket.
 */
public class IndividualTicket extends Ticket {

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
        return discounted(basePrice, discountPercent);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }
}
