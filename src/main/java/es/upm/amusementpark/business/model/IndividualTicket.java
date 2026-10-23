package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for one user. It may stand alone or be part of a group ticket, in
 * which case it carries the group discount.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice) {
        this(id, date, user, basePrice, 0);
    }

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
        return discounted(basePrice, discountPercent);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }

    @Override
    public List<IndividualAccess> getEntryTickets() {
        return List.of(this);
    }
}
