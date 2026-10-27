package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.PassOperationException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Purchase and refund of SpeedPasses and Special Passes.
 */
public class PassService {

    private static final PassService INSTANCE = new PassService();

    private PassRepository passes;
    private TicketRepository tickets;
    private ParkController park;

    private PassService() {
    }

    public static PassService getInstance() {
        return INSTANCE;
    }

    /** Injects the collaborators of this controller; called during application initialization. */
    public void configure(PassRepository passes, TicketRepository tickets, ParkController park) {
        this.passes = passes;
        this.tickets = tickets;
        this.park = park;
    }

    /**
     * Buys 1 or 10 SpeedPasses for an individual ticket, identified
     * {@code purchaseId:1..quantity}. Every identifier is checked first.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, int quantity)
            throws PassOperationException {
        if (quantity != 1 && quantity != 10) {
            throw new PassOperationException("SpeedPasses can only be bought in quantities of 1 or 10.");
        }
        checkIdentifier(purchaseId);
        IndividualTicket ticket = purchasableTicket(ticketId);
        for (int i = 1; i <= quantity; i++) {
            if (passes.exists(purchaseId + ":" + i)) {
                throw new PassOperationException("A pass with identifier " + purchaseId + ":" + i + " already exists.");
            }
        }
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            bought.add(new SpeedPass(purchaseId + ":" + i, ticket));
        }
        for (SpeedPass pass : bought) {
            passes.save(pass);
        }
        return bought;
    }

    /** Buys the Special Pass of an individual ticket that does not have one. */
    public SpecialPass buySpecialPass(String identifier, String ticketId) throws PassOperationException {
        checkIdentifier(identifier);
        if (passes.exists(identifier)) {
            throw new PassOperationException("A pass with identifier " + identifier + " already exists.");
        }
        IndividualTicket ticket = purchasableTicket(ticketId);
        for (Pass existing : passes.findByTicket(ticket)) {
            if (existing instanceof SpecialPass) {
                throw new PassOperationException("Ticket " + ticketId + " already has a Special Pass.");
            }
        }
        SpecialPass pass = new SpecialPass(identifier, ticket);
        passes.save(pass);
        return pass;
    }

    /**
     * Refunds an unconsumed pass of the given type whose ticket is dated after
     * the current date.
     */
    public void refund(String identifier, Class<? extends Pass> type) throws PassOperationException {
        Pass pass = passes.findById(identifier).filter(type::isInstance).orElseThrow(
                () -> new PassOperationException("No " + type.getSimpleName() + " with identifier " + identifier
                        + " exists."));
        if (pass.isConsumed()) {
            throw new PassOperationException("Pass " + identifier + " has been consumed and cannot be refunded.");
        }
        if (!pass.getTicket().isDeletableOn(park.getCurrentDate())) {
            throw new PassOperationException("Pass " + identifier
                    + " belongs to a ticket for the current date or earlier and cannot be refunded.");
        }
        passes.delete(identifier);
    }

    private static void checkIdentifier(String identifier) throws PassOperationException {
        if (identifier.isEmpty()) {
            throw new PassOperationException("The pass identifier cannot be empty.");
        }
    }

    private IndividualTicket purchasableTicket(String ticketId) throws PassOperationException {
        Ticket ticket = tickets.findById(ticketId).orElseThrow(
                () -> new PassOperationException("No ticket with identifier " + ticketId + " exists."));
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new PassOperationException("Ticket " + ticketId + " is not an individual ticket.");
        }
        if (!individual.isActiveOn(park.getCurrentDate())) {
            throw new PassOperationException("Ticket " + ticketId + " is for a past date.");
        }
        return individual;
    }
}
