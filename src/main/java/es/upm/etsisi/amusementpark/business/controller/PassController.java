package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.persistence.PassRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Personal pass operations: SpeedPass purchases (1 or 10), the single Special Pass of a ticket and refunds.
 * Every date check uses the system date supplied by the caller. Rule violations are reported with
 * {@link IllegalArgumentException}.
 */
public class PassController {

    private static final int SINGLE = 1;
    private static final int PACK = 10;

    private static volatile PassController instance;

    /**
     * Creates the unique instance of this controller for the current execution with its injected
     * dependencies. Called once by the composition root at startup; calling it again starts a new execution.
     */
    public static synchronized PassController initialize(PassRepository storage, TicketController tickets) {
        instance = new PassController(storage, tickets);
        return instance;
    }

    /** The unique instance of the current execution. */
    public static PassController getInstance() {
        PassController current = instance;
        if (current == null) {
            throw new IllegalStateException("PassController has not been initialized");
        }
        return current;
    }

    private final PassRepository storage;
    private final TicketController tickets;
    private final List<Pass> passes;

    private PassController(PassRepository storage, TicketController tickets) {
        this.storage = storage;
        this.tickets = tickets;
        this.passes = new ArrayList<>(storage.load(tickets::findIndividualTicket));
    }

    public List<Pass> getPasses() {
        return List.copyOf(passes);
    }

    public Optional<Pass> find(String id) {
        return passes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /** Passes associated with the given individual ticket. */
    public List<Pass> passesOf(Ticket ticket) {
        return passes.stream().filter(p -> p.getTicket() == ticket).toList();
    }

    /** Earliest bought unconsumed pass of that type for the ticket, if any. */
    public Optional<Pass> firstUnused(Ticket ticket, String type) {
        return passesOf(ticket).stream().filter(p -> p.getType().equals(type) && !p.isConsumed()).findFirst();
    }

    /** Marks the passes as consumed and persists them; on failure none stays consumed. */
    public void consume(List<Pass> toConsume) {
        if (toConsume.isEmpty()) {
            return;
        }
        toConsume.forEach(Pass::markConsumed);
        try {
            storage.save(passes);
        } catch (RuntimeException e) {
            toConsume.forEach(Pass::unmarkConsumed);
            throw e;
        }
    }

    /**
     * Deletes every pass associated with the given tickets (cascade of a ticket deletion) and returns them,
     * so that {@link #restore} can undo the deletion if the rest of the cascade fails.
     */
    public List<Pass> removeFor(List<Ticket> deletedTickets) {
        List<Pass> before = new ArrayList<>(passes);
        List<Pass> removed = passes.stream().filter(p -> deletedTickets.stream().anyMatch(t -> t == p.getTicket()))
                .toList();
        if (removed.isEmpty()) {
            return removed;
        }
        passes.removeAll(removed);
        saveOrRevert(() -> {
            passes.clear();
            passes.addAll(before);
        });
        return removed;
    }

    /** Puts back passes removed by {@link #removeFor} (the cascade could not be completed). */
    public void restore(List<Pass> removed, List<Pass> previousOrder) {
        if (removed.isEmpty()) {
            return;
        }
        passes.clear();
        passes.addAll(previousOrder);
        storage.save(passes);
    }

    /** Buys 1 or 10 SpeedPasses {@code <id>-1..n}; all identifiers are checked before anything is created. */
    public List<Pass> buySpeedPasses(String id, String ticketId, int quantity, LocalDate today) {
        require(quantity == SINGLE || quantity == PACK, "SpeedPasses can only be bought in quantities of 1 or 10");
        require(UserValidator.isNotBlank(id), "Pass identifier must not be empty");
        Ticket ticket = usableTicket(ticketId, today);
        List<Pass> bought = new ArrayList<>(quantity);
        for (int i = 1; i <= quantity; i++) {
            String passId = id + "-" + i;
            requireUnused(passId);
            bought.add(new SpeedPass(passId, ticket));
        }
        passes.addAll(bought);
        saveOrRevert(() -> passes.removeAll(bought));
        return bought;
    }

    /** Buys the only Special Pass of a ticket. */
    public Pass buySpecialPass(String id, String ticketId, LocalDate today) {
        require(UserValidator.isNotBlank(id), "Pass identifier must not be empty");
        Ticket ticket = usableTicket(ticketId, today);
        requireUnused(id);
        require(passesOf(ticket).stream().noneMatch(SpecialPass.class::isInstance),
                "Ticket " + ticketId + " already has a Special Pass");
        Pass pass = new SpecialPass(id, ticket);
        passes.add(pass);
        saveOrRevert(() -> passes.remove(pass));
        return pass;
    }

    /** Refunds an unconsumed pass of the given type whose ticket is dated strictly after {@code today}. */
    public Pass refund(String type, String passId, LocalDate today) {
        Pass pass = find(passId).filter(p -> p.getType().equals(type))
                .orElseThrow(() -> new IllegalArgumentException("No " + type + " pass with identifier " + passId));
        require(!pass.isConsumed(), "Pass " + passId + " has been consumed and cannot be refunded");
        require(pass.getTicket().getDate().isAfter(today), "Pass " + passId + " cannot be refunded: its ticket date "
                + pass.getTicket().getDate() + " is not after " + today);
        int index = passes.indexOf(pass);
        passes.remove(index);
        saveOrRevert(() -> passes.add(index, pass));
        return pass;
    }

    private Ticket usableTicket(String ticketId, LocalDate today) {
        Ticket ticket = tickets.findIndividualTicket(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("No individual ticket with identifier " + ticketId));
        require(!ticket.getDate().isBefore(today),
                "Ticket " + ticketId + " is dated " + ticket.getDate() + ", before the system date " + today);
        return ticket;
    }

    private void requireUnused(String passId) {
        require(find(passId).isEmpty(), "A pass with identifier " + passId + " already exists");
    }

    private void saveOrRevert(Runnable revert) {
        try {
            storage.save(passes);
        } catch (RuntimeException e) {
            revert.run();
            throw e;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
