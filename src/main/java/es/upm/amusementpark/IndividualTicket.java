package es.upm.amusementpark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user, either bought on its own or as part of a group ticket's composition.
 */
public class IndividualTicket extends Ticket {

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
}
