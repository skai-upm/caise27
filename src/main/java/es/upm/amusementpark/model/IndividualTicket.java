package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user. The ticket knows its user; users do not know their tickets.
 *
 * <p>Its price is the base price at purchase time minus an optional discount (used when the
 * ticket is part of a group ticket).
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final BigDecimal basePrice;
    private final BigDecimal discount;
    private final boolean partOfGroupTicket;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice) {
        this(id, date, user, basePrice, BigDecimal.ZERO, false);
    }

    IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, BigDecimal discount,
            boolean partOfGroupTicket) {
        super(id, date);
        this.user = user;
        this.basePrice = basePrice;
        this.discount = discount;
        this.partOfGroupTicket = partOfGroupTicket;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Discount applied to the base price, as a fraction (0.20 = 20%).
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    @Override
    public BigDecimal getPrice() {
        return basePrice.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<IndividualTicket> individualTickets() {
        return List.of(this);
    }

    /**
     * Tells whether this ticket belongs to the composition of a group ticket.
     */
    public boolean isPartOfGroupTicket() {
        return partOfGroupTicket;
    }
}
