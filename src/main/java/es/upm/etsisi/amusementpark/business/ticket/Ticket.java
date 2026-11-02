package es.upm.etsisi.amusementpark.business.ticket;

import es.upm.etsisi.amusementpark.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identifier, date of use and price.
 */
public abstract class Ticket implements AccessTicket {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** Whether this ticket is composed of other tickets. */
    public abstract boolean isComposite();

    /** Users admitted when this ticket is used. */
    public List<User> getHolders() {
        return getIndividualTickets().stream().map(IndividualTicket::getUser).toList();
    }

    /** Active tickets are dated on or after the given current date; the rest are historical. */
    public boolean isActiveOn(LocalDate current) {
        return !date.isBefore(current);
    }

    /** A ticket is valid only on its own date. */
    @Override
    public boolean isValidOn(LocalDate current) {
        return date.isEqual(current);
    }

    /** Only tickets dated strictly after the current date may be deleted or refunded. */
    public boolean isDeletableOn(LocalDate current) {
        return date.isAfter(current);
    }
}
