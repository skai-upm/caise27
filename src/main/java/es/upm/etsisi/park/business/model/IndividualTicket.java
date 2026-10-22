package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.model.access.IndividualAccess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user, either bought on its own or as part of a group ticket.
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
        return basePrice.multiply(HUNDRED.subtract(BigDecimal.valueOf(discountPercent)))
                .divide(HUNDRED, basePrice.scale(), RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public IndividualTicket getTicket() {
        return this;
    }

    @Override
    public List<IndividualTicket> getAdmittedTickets() {
        return List.of(this);
    }
}
