package es.upm.amusementpark.business.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;

/**
 * Business rules for buying, refunding and removing SpeedPasses and Special Passes.
 */
public final class PassController {

    private static PassController instance;

    private PassRepository passes;
    private TicketRepository tickets;
    private ParkController park;

    private PassController() {
    }

    public static PassController getInstance() {
        if (instance == null) {
            instance = new PassController();
        }
        return instance;
    }

    /** Injects the dependencies configured at application initialization. */
    public void configure(PassRepository passes, TicketRepository tickets, ParkController park) {
        this.passes = passes;
        this.tickets = tickets;
        this.park = park;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:<n>}; returns the total charged. */
    public BigDecimal buySpeedPasses(String purchaseId, String ticketId, int quantity) throws ParkException {
        if (quantity != 1 && quantity != 10) {
            throw new InvalidDataException("Quantity must be 1 or 10");
        }
        IndividualTicket ticket = purchasableTicket(ticketId);
        List<SpeedPass> bought = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (int n = 1; n <= quantity; n++) {
            String id = purchaseId + ":" + n;
            if (passes.exists(id)) {
                throw new DuplicateException("Pass identifier already exists: " + id);
            }
            SpeedPass pass = new SpeedPass(id, ticket);
            bought.add(pass);
            total = total.add(pass.getPrice());
        }
        passes.addAll(bought);
        return total;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        if (passes.exists(id)) {
            throw new DuplicateException("Pass identifier already exists");
        }
        IndividualTicket ticket = purchasableTicket(ticketId);
        for (Pass pass : passes.findByTicket(ticket)) {
            if (pass instanceof SpecialPass) {
                throw new OperationNotAllowedException("Ticket already has a Special Pass");
            }
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.addAll(List.of(pass));
        return pass;
    }

    public void refundSpeedPass(String id) throws ParkException {
        refund(id, SpeedPass.class, "SpeedPass not found");
    }

    public void refundSpecialPass(String id) throws ParkException {
        refund(id, SpecialPass.class, "Special Pass not found");
    }

    /** Removes every pass of the given tickets (joint refund when the tickets are deleted). */
    public void removePassesOf(Collection<IndividualTicket> deletedTickets) {
        for (IndividualTicket ticket : deletedTickets) {
            for (Pass pass : passes.findByTicket(ticket)) {
                passes.remove(pass);
            }
        }
    }

    private void refund(String id, Class<? extends Pass> type, String notFound) throws ParkException {
        Pass pass = passes.findById(id);
        if (!type.isInstance(pass)) {
            throw new NotFoundException(notFound);
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Consumed passes cannot be refunded");
        }
        if (!pass.isRefundable(park.getCurrentDate())) {
            throw new OperationNotAllowedException("Only passes of tickets dated after the current date can be refunded");
        }
        passes.remove(pass);
    }

    private IndividualTicket purchasableTicket(String ticketId) throws ParkException {
        IndividualTicket ticket = tickets.findIndividualTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("Individual ticket not found");
        }
        if (ticket.getDate().isBefore(park.getCurrentDate())) {
            throw new OperationNotAllowedException("Passes cannot be bought for past tickets");
        }
        return ticket;
    }
}
