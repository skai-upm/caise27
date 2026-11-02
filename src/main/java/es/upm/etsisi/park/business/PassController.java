package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SpeedPass and Special Pass purchase, refund and lookup.
 */
public final class PassController {

    private static final PassController INSTANCE = new PassController();
    private static final int SINGLE = 1;
    private static final int PACK = 10;

    private Park park;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:<n>}; nothing is created on any conflict. */
    public List<SpeedPass> createSpeedPasses(String purchaseId, String ticketId, String quantity) {
        Park.requireNotBlank(purchaseId);
        int count = parseQuantity(quantity);
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        BigDecimal unit = SpeedPass.priceFor(ticket);
        List<SpeedPass> created = new ArrayList<>();
        for (int n = 1; n <= count; n++) {
            String id = purchaseId + ":" + n;
            requireFreeId(id);
            created.add(new SpeedPass(id, ticket, unit, false));
        }
        Map<String, Pass> passes = park.passes();
        created.forEach(p -> passes.put(p.getId(), p));
        park.commit(() -> created.forEach(p -> passes.remove(p.getId())));
        return created;
    }

    public SpecialPass createSpecialPass(String id, String ticketId) {
        Park.requireNotBlank(id);
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        requireFreeId(id);
        if (findSpecialPass(ticket) != null) {
            throw new ConflictException("Ticket already has a Special Pass: " + ticketId);
        }
        SpecialPass pass = new SpecialPass(id, ticket, SpecialPass.priceFor(ticket), false);
        Map<String, Pass> passes = park.passes();
        passes.put(id, pass);
        park.commit(() -> passes.remove(id));
        return pass;
    }

    public void deleteSpeedPass(String id) {
        refund(id, SpeedPass.class, "SpeedPass");
    }

    public void deleteSpecialPass(String id) {
        refund(id, SpecialPass.class, "Special Pass");
    }

    /** First unconsumed SpeedPass of the ticket, or {@code null}. */
    public SpeedPass findUnconsumedSpeedPass(IndividualTicket ticket) {
        for (Pass p : park.passesOf(ticket)) {
            if (p instanceof SpeedPass s && !s.isConsumed()) {
                return s;
            }
        }
        return null;
    }

    /** The ticket's Special Pass, consumed or not, or {@code null}. */
    public SpecialPass findSpecialPass(IndividualTicket ticket) {
        for (Pass p : park.passesOf(ticket)) {
            if (p instanceof SpecialPass s) {
                return s;
            }
        }
        return null;
    }

    /**
     * Removes from memory every pass of the given tickets without saving, returning the action that
     * puts them back; used by ticket deletion so the cascade is saved and undone as one operation.
     */
    Runnable detachPassesOf(List<IndividualTicket> tickets) {
        Map<String, Pass> passes = park.passes();
        Map<String, Pass> before = new LinkedHashMap<>(passes);
        passes.values().removeIf(p -> tickets.contains(p.getTicket()));
        return () -> Park.restore(passes, before);
    }

    private void refund(String id, Class<? extends Pass> kind, String label) {
        Pass pass = park.findPass(id);
        if (!kind.isInstance(pass)) {
            throw new NotFoundException(label + " not found: " + id);
        }
        if (pass.isConsumed()) {
            throw new RuleViolationException(label + " already consumed: " + id);
        }
        if (!pass.getTicket().isDeletableOn(park.getToday())) {
            throw new RuleViolationException(label + " can be refunded only for tickets dated after "
                    + ValueParser.formatDate(park.getToday()));
        }
        Map<String, Pass> passes = park.passes();
        Map<String, Pass> before = new LinkedHashMap<>(passes);
        passes.remove(id);
        park.commit(() -> Park.restore(passes, before));
    }

    private IndividualTicket requirePurchasableTicket(String ticketId) {
        Ticket ticket = park.findTicket(ticketId);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new NotFoundException("Individual ticket not found: " + ticketId);
        }
        if (!individual.isActiveOn(park.getToday())) {
            throw new RuleViolationException("Ticket date is in the past: " + ticketId);
        }
        return individual;
    }

    private void requireFreeId(String id) {
        if (park.findPass(id) != null) {
            throw new ConflictException("Pass identifier already in use: " + id);
        }
    }

    private static int parseQuantity(String quantity) {
        if (Integer.toString(SINGLE).equals(quantity)) {
            return SINGLE;
        }
        if (Integer.toString(PACK).equals(quantity)) {
            return PACK;
        }
        throw new RuleViolationException("Quantity must be 1 or 10: " + quantity);
    }
}
