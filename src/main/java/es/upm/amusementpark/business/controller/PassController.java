package es.upm.amusementpark.business.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;

/**
 * Single controller for SpeedPass and Special Pass purchases and refunds. It collaborates with the
 * injected {@link ParkController} to find tickets, read the current date and persist changes.
 */
public final class PassController {

    private static final PassController INSTANCE = new PassController();

    private ParkController park;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController parkController) {
        this.park = parkController;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:<n>}; returns the passes created. */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) {
        requireNonEmpty(purchaseId, "purchase identifier");
        int count = switch (quantity) {
            case "1" -> 1;
            case "10" -> 10;
            default -> throw new ValidationException("quantity must be 1 or 10: " + quantity);
        };
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ids.add(purchaseId + ":" + i);
        }
        ids.forEach(this::requireFreePassId);
        List<SpeedPass> created = new ArrayList<>();
        BigDecimal unitPrice = SpeedPass.priceFor(ticket);
        for (String id : ids) {
            SpeedPass pass = new SpeedPass(id, unitPrice, ticket, false);
            ticket.addSpeedPass(pass);
            created.add(pass);
        }
        park.saveAll();
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) {
        requireNonEmpty(id, "pass identifier");
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        requireFreePassId(id);
        if (ticket.getSpecialPass().isPresent()) {
            throw new OperationNotAllowedException("ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(id, SpecialPass.priceFor(ticket), ticket, false);
        ticket.setSpecialPass(pass);
        park.saveAll();
        return pass;
    }

    public void refundSpeedPass(String id) {
        if (!(park.findPass(id) instanceof SpeedPass pass)) {
            throw new NotFoundException("SpeedPass not found: " + id);
        }
        requireRefundable(pass);
        pass.getTicket().removeSpeedPass(pass);
        park.saveAll();
    }

    public void refundSpecialPass(String id) {
        if (!(park.findPass(id) instanceof SpecialPass pass)) {
            throw new NotFoundException("Special Pass not found: " + id);
        }
        requireRefundable(pass);
        pass.getTicket().setSpecialPass(null);
        park.saveAll();
    }

    /** Sum of the prices of the given passes. */
    public static BigDecimal total(List<? extends Pass> passes) {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Pass p : passes) {
            total = total.add(p.getPrice());
        }
        return total;
    }

    private IndividualTicket requirePurchasableTicket(String ticketId) {
        IndividualTicket ticket = park.requireIndividualTicket(ticketId);
        if (ticket.getDate().isBefore(park.getCurrentDate())) {
            throw new OperationNotAllowedException("ticket " + ticketId + " is for a past date");
        }
        return ticket;
    }

    private void requireFreePassId(String id) {
        if (park.findPass(id) != null) {
            throw new DuplicateException("pass identifier already in use: " + id);
        }
    }

    private void requireRefundable(Pass pass) {
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("pass " + pass.getId() + " is consumed and cannot be refunded");
        }
        if (!pass.getTicket().isDeletable(park.getCurrentDate())) {
            throw new OperationNotAllowedException("pass " + pass.getId()
                    + " belongs to a ticket for today or a past date and cannot be refunded");
        }
    }

    private static void requireNonEmpty(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(name + " must not be empty");
        }
    }
}
