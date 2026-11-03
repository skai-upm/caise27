package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SpeedPass and Special Pass purchases and refunds. Passes share their own
 * identifier space, separate from tickets.
 */
public final class PassController implements PassLedger {

    private static final PassController INSTANCE = new PassController();

    private Park park;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}; all or none. */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantityText) throws ParkException {
        GroupController.requireText(purchaseId, "purchase identifier");
        int quantity = switch (quantityText) {
            case "1" -> 1;
            case "10" -> 10;
            default -> throw new ValidationException("invalid quantity " + quantityText + ": it must be 1 or 10");
        };
        IndividualTicket ticket = purchasableTicket(ticketId);
        BigDecimal price = SpeedPass.priceFor(ticket);
        List<SpeedPass> created = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String id = purchaseId + ":" + i;
            requireFreeId(id);
            created.add(new SpeedPass(id, ticket, price, false));
        }
        created.forEach(park::addPass);
        park.commit(() -> created.forEach(park::removePass));
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        GroupController.requireText(id, "pass identifier");
        requireFreeId(id);
        IndividualTicket ticket = purchasableTicket(ticketId);
        SpecialPass existing = specialPassOf(ticket);
        if (existing != null) {
            throw new OperationNotAllowedException("ticket " + ticketId + " already has Special Pass " + existing.getId());
        }
        SpecialPass pass = new SpecialPass(id, ticket, SpecialPass.priceFor(ticket), false);
        park.addPass(pass);
        park.commit(() -> park.removePass(pass));
        return pass;
    }

    public void refundSpeedPass(String id) throws ParkException {
        refund(id, SpeedPass.class, "SpeedPass");
    }

    public void refundSpecialPass(String id) throws ParkException {
        refund(id, SpecialPass.class, "Special Pass");
    }

    private void refund(String id, Class<? extends Pass> type, String label) throws ParkException {
        Pass pass = park.findPass(id);
        if (!type.isInstance(pass)) {
            throw new NotFoundException("no " + label + " with identifier " + id);
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException(label + " " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.getTicket().getDate().isAfter(park.getCurrentDate())) {
            throw new OperationNotAllowedException(label + " " + id
                    + " belongs to a ticket that is not dated after the current system date");
        }
        park.removePass(pass);
        park.commit(() -> park.addPass(pass));
    }

    @Override
    public SpeedPass unusedSpeedPass(IndividualTicket ticket) {
        for (Pass p : park.passesOf(ticket)) {
            if (p instanceof SpeedPass s && !s.isConsumed()) {
                return s;
            }
        }
        return null;
    }

    @Override
    public SpecialPass specialPassOf(IndividualTicket ticket) {
        for (Pass p : park.passesOf(ticket)) {
            if (p instanceof SpecialPass s) {
                return s;
            }
        }
        return null;
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        return park.passesOf(ticket);
    }

    /** Removes from the park every pass of the given tickets and returns them (for cascades). */
    List<Pass> detachPassesOf(List<IndividualTicket> tickets) {
        List<Pass> removed = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            removed.addAll(park.passesOf(t));
        }
        removed.forEach(park::removePass);
        return removed;
    }

    void reattach(List<Pass> passes) {
        passes.forEach(park::addPass);
    }

    private IndividualTicket purchasableTicket(String ticketId) throws ParkException {
        Ticket ticket = park.findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("no ticket with identifier " + ticketId);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new OperationNotAllowedException("passes can only be bought for individual tickets, not for " + ticketId);
        }
        if (ticket.getDate().isBefore(park.getCurrentDate())) {
            throw new OperationNotAllowedException("ticket " + ticketId + " is dated before the current system date");
        }
        return individual;
    }

    private void requireFreeId(String id) throws ValidationException {
        if (park.findPass(id) != null) {
            throw new ValidationException("pass identifier " + id + " is already in use");
        }
    }
}
