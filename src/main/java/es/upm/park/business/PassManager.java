package es.upm.park.business;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.Result;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.User;
import es.upm.park.persistence.PassFileStore;
import es.upm.park.persistence.PassRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Sells and refunds personal passes attached to individual tickets (one instance per execution).
 * Every successful change is saved before it becomes visible in memory.
 */
public final class PassManager {

    private static final PassManager INSTANCE = new PassManager();
    private static final String PACK_OF_ONE = "1";
    private static final String PACK_OF_TEN = "10";

    private PassRepository store;
    private TicketManager tickets;
    private List<Pass> passes = new ArrayList<>();
    private List<String> historicalLines = new ArrayList<>();
    private Set<String> historicalIds = new HashSet<>();

    private PassManager() {
    }

    /** The single pass manager of the execution. */
    public static PassManager getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and the ticket manager into the single instance and loads the
     * passes. Called at startup by {@link ApplicationContext}.
     */
    public static PassManager configure(PassRepository store, TicketManager tickets) throws IOException {
        PassFileStore.Contents contents = store.load(tickets::findIndividualTicket);
        Set<String> reserved = new HashSet<>();
        for (String line : contents.historicalLines()) {
            reserved.add(PassFileStore.idOf(line));
        }
        INSTANCE.store = store;
        INSTANCE.tickets = tickets;
        INSTANCE.passes = new ArrayList<>(contents.passes());
        INSTANCE.historicalLines = new ArrayList<>(contents.historicalLines());
        INSTANCE.historicalIds = reserved;
        return INSTANCE;
    }

    public List<Pass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public Optional<Pass> find(String id) {
        return passes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /** Passes attached to any individual ticket of the user. */
    public List<Pass> passesOf(User user) {
        return passes.stream().filter(p -> p.getTicket().getUser() == user).toList();
    }

    /** Buys 1 or 10 SpeedPasses with identifiers {@code purchaseId-1..n}; all or nothing. */
    public Result buySpeedPasses(String purchaseId, String ticketId, String quantity, LocalDate today) {
        if (!PACK_OF_ONE.equals(quantity) && !PACK_OF_TEN.equals(quantity)) {
            return Result.error("SpeedPasses can only be bought in quantities of 1 or 10.");
        }
        Result invalid = checkTicket(ticketId, today);
        if (invalid != null) {
            return invalid;
        }
        IndividualTicket ticket = tickets.findIndividualTicket(ticketId).orElseThrow();
        int count = Integer.parseInt(quantity);
        List<Pass> bought = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String id = purchaseId + "-" + i;
            if (isIdTaken(id)) {
                return Result.error("A pass with identifier " + id + " already exists; nothing was purchased.");
            }
            bought.add(new SpeedPass(id, SpeedPass.priceFor(ticket), ticket, false));
        }
        BigDecimal total = bought.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Pass> candidate = new ArrayList<>(passes);
        candidate.addAll(bought);
        String range = count == 1 ? bought.get(0).getId() : bought.get(0).getId() + ".." + bought.get(count - 1).getId();
        return commit(candidate, "SpeedPasses " + range + " purchased for ticket " + ticketId
                + ". Total: " + total.toPlainString());
    }

    /** Buys the single Special Pass of a ticket. */
    public Result buySpecialPass(String id, String ticketId, LocalDate today) {
        Result invalid = checkTicket(ticketId, today);
        if (invalid != null) {
            return invalid;
        }
        IndividualTicket ticket = tickets.findIndividualTicket(ticketId).orElseThrow();
        if (passes.stream().anyMatch(p -> p instanceof SpecialPass && p.getTicket() == ticket)) {
            return Result.error("Ticket " + ticketId + " already has a Special Pass.");
        }
        if (isIdTaken(id)) {
            return Result.error("A pass with identifier " + id + " already exists.");
        }
        Pass pass = new SpecialPass(id, SpecialPass.priceFor(ticket), ticket, false);
        List<Pass> candidate = new ArrayList<>(passes);
        candidate.add(pass);
        return commit(candidate, "Special Pass " + id + " purchased for ticket " + ticketId
                + ". Total: " + pass.getPrice().toPlainString());
    }

    /** Refunds an unconsumed pass of the given kind whose ticket date is after {@code today}. */
    public Result refund(String id, String kind, LocalDate today) {
        Optional<Pass> pass = find(id).filter(p -> p.kind().equals(kind));
        if (pass.isEmpty()) {
            return Result.error("No " + kind + " pass with identifier " + id + ".");
        }
        if (pass.get().isConsumed()) {
            return Result.error("Pass " + id + " has been consumed and cannot be refunded.");
        }
        if (!pass.get().getTicket().getDate().isAfter(today)) {
            return Result.error("Pass " + id + " can only be refunded if its ticket date is later than the system date "
                    + today + ".");
        }
        List<Pass> candidate = new ArrayList<>(passes);
        candidate.remove(pass.get());
        return commit(candidate, "Pass " + id + " refunded: " + pass.get().getPrice().toPlainString());
    }

    /** First unused SpeedPass of the ticket, if any. */
    public Optional<Pass> findAvailableSpeedPass(IndividualTicket ticket) {
        return passes.stream().filter(p -> p instanceof SpeedPass && p.getTicket() == ticket && !p.isConsumed())
                .findFirst();
    }

    /** The ticket's Special Pass if it is still unused. */
    public Optional<Pass> findAvailableSpecialPass(IndividualTicket ticket) {
        return passes.stream().filter(p -> p instanceof SpecialPass && p.getTicket() == ticket && !p.isConsumed())
                .findFirst();
    }

    /** Marks the passes as consumed and saves; on a save failure no pass stays marked. */
    public Result consume(List<Pass> toConsume) {
        List<Pass> marked = new ArrayList<>();
        for (Pass p : toConsume) {
            if (!p.isConsumed()) {
                p.consume();
                marked.add(p);
            }
        }
        try {
            store.save(passes, historicalLines);
        } catch (IOException e) {
            marked.forEach(Pass::restoreUnused);
            return Result.error("Could not save passes: " + e.getMessage());
        }
        return Result.ok(marked.size() + " pass(es) consumed.");
    }

    private Result checkTicket(String ticketId, LocalDate today) {
        Optional<IndividualTicket> ticket = tickets.findIndividualTicket(ticketId);
        if (ticket.isEmpty()) {
            return Result.error("Individual ticket " + ticketId + " does not exist.");
        }
        if (ticket.get().getDate().isBefore(today)) {
            return Result.error("Ticket " + ticketId + " is dated before the system date " + today + ".");
        }
        return null;
    }

    private boolean isIdTaken(String id) {
        return historicalIds.contains(id) || find(id).isPresent();
    }

    private Result commit(List<Pass> candidate, String message) {
        try {
            store.save(candidate, historicalLines);
        } catch (IOException e) {
            return Result.error("Could not save passes: " + e.getMessage());
        }
        passes = candidate;
        return Result.ok(message);
    }
}
