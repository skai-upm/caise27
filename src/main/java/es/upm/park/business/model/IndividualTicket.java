package es.upm.park.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for a single user. The ticket knows its user (by email); users do not
 * keep references to their tickets. It may be bought on its own or be part of
 * the composition of a group ticket.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final String ownerEmail;
    private final boolean partOfGroupTicket;

    public IndividualTicket(String id, String ownerEmail, LocalDate date, BigDecimal price) {
        this(id, ownerEmail, date, price, false);
    }

    IndividualTicket(String id, String ownerEmail, LocalDate date, BigDecimal price, boolean partOfGroupTicket) {
        super(id, date, price);
        this.ownerEmail = ownerEmail;
        this.partOfGroupTicket = partOfGroupTicket;
    }

    /** True when this ticket belongs to the composition of a group ticket. */
    public boolean isPartOfGroupTicket() {
        return partOfGroupTicket;
    }

    @Override
    public String getOwnerEmail() {
        return ownerEmail;
    }

    @Override
    public List<String> admittedUsers() {
        return List.of(ownerEmail);
    }
}
