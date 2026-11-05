package es.upm.parque.business;

import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;

/**
 * Business operations on passes: purchase of SpeedPasses and Special Passes for individual tickets, and refunds.
 */
public class PassManager {

    private static final String SINGLE = "1";
    private static final String PACKAGE = "10";

    private ParkController park;

    private PassManager() {
    }

    private static final class Holder {
        private static final PassManager INSTANCE = new PassManager();
    }

    /** Returns the only pass management instance. */
    public static PassManager getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management configured during application initialization. */
    public void inject(ParkController park) {
        this.park = park;
    }

    private ParkController park() {
        return ParkConfiguration.requireInjected(park);
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}; every identifier is checked before any pass
     * is created, so a conflict creates nothing.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantityText)
            throws ParkException {
        if (purchaseId.isEmpty()) {
            throw new InvalidDataException("the purchase identifier cannot be empty");
        }
        if (!SINGLE.equals(quantityText) && !PACKAGE.equals(quantityText)) {
            throw new InvalidDataException("invalid quantity '" + quantityText + "' (allowed: 1 or 10)");
        }
        IndividualTicket ticket = purchasableTicket(ticketId);
        int quantity = Integer.parseInt(quantityText);
        List<String> ids = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = SpeedPass.generatedId(purchaseId, n);
            if (park().getPasses().exists(id)) {
                throw new RuleViolationException("pass identifier " + id + " is already in use; nothing was created");
            }
            ids.add(id);
        }
        List<SpeedPass> created = ids.stream().map(id -> new SpeedPass(id, ticket)).toList();
        created.forEach(park().getPasses()::add);
        park().persist();
        return created;
    }

    /** Buys the Special Pass of the ticket; a ticket can never have two, even if the first one was consumed. */
    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        if (id.isEmpty()) {
            throw new InvalidDataException("the pass identifier cannot be empty");
        }
        IndividualTicket ticket = purchasableTicket(ticketId);
        if (park().getPasses().exists(id)) {
            throw new RuleViolationException("a pass with identifier " + id + " already exists");
        }
        SpecialPass existing = park().getPasses().specialPassOf(ticket);
        if (existing != null) {
            throw new RuleViolationException("ticket " + ticketId + " already has Special Pass " + existing.getId()
                    + (existing.isConsumed() ? " (consumed)" : ""));
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        park().getPasses().add(pass);
        park().persist();
        return pass;
    }

    /** Refunds (deletes) an unconsumed pass of the given type whose ticket is dated after the current date. */
    public <P extends Pass> P refund(String id, Class<P> type, String typeName) throws ParkException {
        Pass found = park().getPasses().findById(id);
        if (!type.isInstance(found)) {
            throw new NotFoundException("no " + typeName + " with identifier " + id);
        }
        P pass = type.cast(found);
        if (pass.isConsumed()) {
            throw new RuleViolationException(typeName + " " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(park().getCurrentDate())) {
            throw new RuleViolationException(typeName + " " + id + " belongs to ticket " + pass.getTicket().getId()
                    + ", valid today or earlier, and cannot be refunded");
        }
        park().getPasses().remove(id);
        park().persist();
        return pass;
    }

    private IndividualTicket purchasableTicket(String ticketId) throws ParkException {
        if (!(park().getTickets().findById(ticketId) instanceof IndividualTicket ticket)) {
            throw new NotFoundException("no individual ticket with identifier " + ticketId);
        }
        if (ticket.getDate().isBefore(park().getCurrentDate())) {
            throw new RuleViolationException("ticket " + ticketId + " is dated " + ticket.getDate()
                    + ", before the current date " + park().getCurrentDate());
        }
        return ticket;
    }
}
