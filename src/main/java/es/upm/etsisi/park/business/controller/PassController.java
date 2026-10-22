package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.attraction.PassLookup;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.persistence.PassRepository;
import es.upm.etsisi.park.persistence.TicketRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SpeedPass and Special Pass purchases and refunds. Also answers attractions' pass
 * lookups. Single instance per execution.
 */
public final class PassController implements PassLookup {

    private static final PassController INSTANCE = new PassController();
    private static final String NAME = "PassController";
    private static final String DERIVED_ID_SEPARATOR = ":";

    private PassRepository passes;
    private TicketRepository tickets;
    private SystemState state;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(PassRepository passes, TicketRepository tickets, SystemState state) {
        this.passes = passes;
        this.tickets = tickets;
        this.state = state;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}; all or nothing. */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantityText) {
        Checks.configured(passes, NAME);
        Checks.requireText(purchaseId, "Purchase identifier");
        int quantity = parseQuantity(quantityText);
        IndividualTicket ticket = eligibleTicket(ticketId);
        List<SpeedPass> bought = new ArrayList<>();
        for (int number = 1; number <= quantity; number++) {
            String id = purchaseId + DERIVED_ID_SEPARATOR + number;
            requireFreeId(id);
            bought.add(SpeedPass.purchase(id, ticket));
        }
        passes.addAll(bought);
        return bought;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) {
        Checks.configured(passes, NAME);
        Checks.requireText(id, "Pass identifier");
        requireFreeId(id);
        IndividualTicket ticket = eligibleTicket(ticketId);
        Checks.require(specialPass(ticket).isEmpty(),
                () -> new RuleViolationException("Ticket " + ticketId + " already has a Special Pass"));
        SpecialPass pass = SpecialPass.purchase(id, ticket);
        passes.add(pass);
        return pass;
    }

    public void refundSpeedPass(String id) {
        refund(id, SpeedPass.class);
    }

    public void refundSpecialPass(String id) {
        refund(id, SpecialPass.class);
    }

    @Override
    public Optional<SpeedPass> availableSpeedPass(IndividualTicket ticket) {
        for (Pass pass : passes.findByTicket(ticket)) {
            if (pass instanceof SpeedPass speed && !speed.isConsumed()) {
                return Optional.of(speed);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<SpecialPass> specialPass(IndividualTicket ticket) {
        for (Pass pass : passes.findByTicket(ticket)) {
            if (pass instanceof SpecialPass special) {
                return Optional.of(special);
            }
        }
        return Optional.empty();
    }

    public List<Pass> findByTicket(IndividualTicket ticket) {
        return Checks.configured(passes, NAME).findByTicket(ticket);
    }

    private <P extends Pass> void refund(String id, Class<P> kind) {
        Pass pass = Checks.configured(passes, NAME).findById(id)
                .filter(kind::isInstance)
                .orElseThrow(() -> new NotFoundException(kindName(kind) + " not found: " + id));
        Checks.require(!pass.isConsumed(), () -> new RuleViolationException("Pass " + id + " has been consumed"));
        Checks.require(pass.isRefundableOn(state.getCurrentDate()), () -> new RuleViolationException(
                "Only passes of tickets dated after the current date (" + state.getCurrentDate() + ") can be refunded"));
        passes.remove(pass);
    }

    private IndividualTicket eligibleTicket(String ticketId) {
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
        Checks.require(ticket instanceof IndividualTicket,
                () -> new RuleViolationException("Passes can only be bought for individual tickets"));
        Checks.require(ticket.isActiveOn(state.getCurrentDate()), () -> new RuleViolationException(
                "Ticket " + ticketId + " is dated before the current date (" + state.getCurrentDate() + ")"));
        return (IndividualTicket) ticket;
    }

    private void requireFreeId(String id) {
        Checks.require(passes.findById(id).isEmpty(), () -> new DuplicateException("Pass identifier already in use: " + id));
    }

    private static int parseQuantity(String text) {
        Checks.require("1".equals(text) || "10".equals(text),
                () -> new ValidationException("Quantity must be 1 or 10: " + text));
        return Integer.parseInt(text);
    }

    private static String kindName(Class<? extends Pass> kind) {
        return kind == SpeedPass.class ? "SpeedPass" : "Special Pass";
    }
}
