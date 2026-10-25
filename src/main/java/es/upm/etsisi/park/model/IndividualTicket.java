package es.upm.etsisi.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket for a single user. Also used as the components of a {@link GroupTicket}.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final int discountPercent;

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user) {
        this(id, date, basePrice, user, 0);
    }

    IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user, int discountPercent) {
        super(id, date, basePrice);
        this.user = user;
        this.discountPercent = discountPercent;
    }

    public User getUser() {
        return user;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public BigDecimal getPrice() {
        return discounted(getBasePrice(), discountPercent);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }
}
