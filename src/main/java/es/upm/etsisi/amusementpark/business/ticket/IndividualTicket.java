package es.upm.etsisi.amusementpark.business.ticket;

import es.upm.etsisi.amusementpark.business.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user. It references its user; the user does not
 * reference its tickets.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;
    private final BigDecimal basePrice;
    private final BigDecimal discount;

    /**
     * @param discount fraction of the base price discounted, e.g. 0.20 for 20 %
     */
    public IndividualTicket(String identifier, LocalDate date, User user, BigDecimal basePrice, BigDecimal discount) {
        super(identifier, date);
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

    public BigDecimal getDiscount() {
        return discount;
    }

    @Override
    public BigDecimal getPrice() {
        return basePrice.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public IndividualTicket getIndividualTicket() {
        return this;
    }

    @Override
    public List<IndividualTicket> getEntryTickets() {
        return List.of(this);
    }
}
