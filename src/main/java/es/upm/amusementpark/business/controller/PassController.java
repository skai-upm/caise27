package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassWallet;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SpeedPass and Special Pass purchases and refunds (Singleton). Also acts as the pass wallet used by attractions.
 */
public final class PassController implements PassWallet {

    private static PassController instance;

    private Park park;
    private PassRepository passes;
    private TicketRepository tickets;

    private PassController() {
    }

    public static synchronized PassController getInstance() {
        if (instance == null) {
            instance = new PassController();
        }
        return instance;
    }

    public void configure(Park park, PassRepository passes, TicketRepository tickets) {
        this.park = park;
        this.passes = passes;
        this.tickets = tickets;
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}. Nothing is created if any identifier is taken.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantityText) {
        if (!quantityText.equals("1") && !quantityText.equals("10")) {
            throw new InvalidDataException("invalid quantity " + quantityText + " (allowed: 1 or 10)");
        }
        int quantity = Integer.parseInt(quantityText);
        IndividualTicket ticket = purchasableTicket(ticketId);
        List<SpeedPass> bought = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = purchaseId + ":" + n;
            requireUnusedId(id);
            bought.add(SpeedPass.purchase(id, ticket));
        }
        passes.addAll(bought);
        return bought;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) {
        IndividualTicket ticket = purchasableTicket(ticketId);
        requireUnusedId(id);
        if (specialPassOf(ticket) != null) {
            throw new OperationNotAllowedException("ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = SpecialPass.purchase(id, ticket);
        passes.addAll(List.of(pass));
        return pass;
    }

    public SpeedPass refundSpeedPass(String id) {
        return refund(id, SpeedPass.class, "SpeedPass");
    }

    public SpecialPass refundSpecialPass(String id) {
        return refund(id, SpecialPass.class, "Special Pass");
    }

    /**
     * Removes every pass of the given tickets (used when those tickets are deleted).
     */
    public void removePassesOf(Collection<IndividualTicket> ownerTickets) {
        List<Pass> removed = new ArrayList<>();
        for (IndividualTicket ticket : ownerTickets) {
            removed.addAll(passes.passesOf(ticket));
        }
        passes.removeAll(removed);
    }

    /**
     * Persists consumption state after an attraction entry.
     */
    public void recordConsumption() {
        passes.save();
    }

    @Override
    public SpeedPass availableSpeedPass(IndividualTicket ticket) {
        for (Pass pass : passes.passesOf(ticket)) {
            if (pass instanceof SpeedPass speedPass && !pass.isConsumed()) {
                return speedPass;
            }
        }
        return null;
    }

    @Override
    public SpecialPass specialPassOf(IndividualTicket ticket) {
        for (Pass pass : passes.passesOf(ticket)) {
            if (pass instanceof SpecialPass specialPass) {
                return specialPass;
            }
        }
        return null;
    }

    private <P extends Pass> P refund(String id, Class<P> type, String label) {
        Pass pass = passes.findById(id);
        if (!type.isInstance(pass)) {
            throw new NotFoundException("no " + label + " with identifier " + id);
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException(label + " " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(park.getDate())) {
            throw new OperationNotAllowedException(label + " " + id + " belongs to ticket " + pass.getTicket().getId()
                    + ", which is for the current day or earlier");
        }
        passes.removeAll(List.of(pass));
        return type.cast(pass);
    }

    private IndividualTicket purchasableTicket(String ticketId) {
        Ticket ticket = tickets.findById(ticketId);
        if (ticket == null) {
            throw new NotFoundException("no ticket with identifier " + ticketId);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new InvalidDataException("ticket " + ticketId + " is not an individual ticket");
        }
        if (!individual.isActiveOn(park.getDate())) {
            throw new OperationNotAllowedException("ticket " + ticketId + " is dated before the current date");
        }
        return individual;
    }

    private void requireUnusedId(String id) {
        if (passes.exists(id)) {
            throw new DuplicateException("a pass with identifier " + id + " already exists");
        }
    }
}
