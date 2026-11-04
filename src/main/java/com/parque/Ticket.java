package com.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * An individual ticket for one date of use, owned by a registered user (a reference, not a copy).
 * Immutable: the price captured at purchase never changes.
 */
public class Ticket {
    private final String id;
    private final User user;
    private final String holderDni;
    private final LocalDate date;
    private final BigDecimal price;

    /** Ticket owned by a registered user. */
    public Ticket(String id, User user, LocalDate date, BigDecimal price) {
        this(id, user, user.getDni(), date, price);
    }

    /**
     * Ticket whose owner may no longer be registered ({@code user == null}); kept as a historical record
     * identified by the holder's DNI.
     */
    public Ticket(String id, User user, String holderDni, LocalDate date, BigDecimal price) {
        this.id = id;
        this.user = user;
        this.holderDni = holderDni;
        this.date = date;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    /** The owner, or null for the history of a user deleted in an earlier execution. */
    public User getUser() {
        return user;
    }

    public String getHolderDni() {
        return holderDni;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** Price with two decimals, e.g. {@code 40.00}. */
    public String getPriceText() {
        return price.toPlainString();
    }
}
