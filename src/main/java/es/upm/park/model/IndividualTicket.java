package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A ticket purchased for a single user. The ticket knows its user (by email);
 * users do not keep references to their tickets.
 */
public class IndividualTicket extends Ticket {

    private final String ownerEmail;

    public IndividualTicket(String id, String ownerEmail, LocalDate date, BigDecimal price) {
        super(id, date, price);
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }
}
