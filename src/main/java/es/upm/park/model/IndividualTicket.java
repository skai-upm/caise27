package es.upm.park.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for a single user. Standalone tickets have no discount; tickets
 * composing a group ticket carry the group discount.
 */
public class IndividualTicket extends Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final User user;
    private final BigDecimal basePrice;
    private final GroupDiscount discount;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice) {
        this(id, date, user, basePrice, GroupDiscount.NONE);
    }

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, GroupDiscount discount) {
        super(id, date);
        this.user = user;
        this.basePrice = basePrice;
        this.discount = discount;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public GroupDiscount getDiscount() {
        return discount;
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discount.getPercent()));
        return basePrice.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }
}
