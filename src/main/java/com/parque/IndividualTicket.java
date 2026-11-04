package com.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * An individual ticket for one date of use, owned by a registered user (a reference, not a copy).
 * Immutable: the price captured at purchase never changes. It may be a constituent of a {@link GroupTicket}.
 */
public class IndividualTicket extends Ticket {
    private final User user;
    private final String holderDni;
    private final BigDecimal price;

    /** Ticket owned by a registered user. */
    public IndividualTicket(String id, User user, LocalDate date, BigDecimal price) {
        this(id, user, user.getDni(), date, price);
    }

    /**
     * Ticket whose owner may no longer be registered ({@code user == null}); kept as a historical record
     * identified by the holder's DNI.
     */
    public IndividualTicket(String id, User user, String holderDni, LocalDate date, BigDecimal price) {
        super(id, date);
        this.user = user;
        this.holderDni = holderDni;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    /** The owner, or null for the history of a user deleted in an earlier execution. */
    public User getUser() {
        return user;
    }

    public String getHolderDni() {
        return holderDni;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }
}
