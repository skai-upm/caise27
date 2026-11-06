package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Information and behavior shared by every ticket type: a unique identifier,
 * a date of use and a price. Whether a ticket is active is derived from its
 * date and the current system date.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String id, LocalDate date, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** Active tickets are dated on or after the current date. */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after the current date may be deleted. */
    public boolean isDeletable(LocalDate today) {
        return date.isAfter(today);
    }

    /** Identifiers this ticket occupies in the shared ticket identifier space. */
    public List<String> ids() {
        return List.of(id);
    }

    /** Whether this ticket concerns the given user. */
    public abstract boolean involves(User user);
}
