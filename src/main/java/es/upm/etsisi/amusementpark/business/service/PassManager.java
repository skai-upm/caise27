package es.upm.etsisi.amusementpark.business.service;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.persistence.PassFileRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for SpeedPasses and Special Passes: purchases (all or
 * nothing), refunds and lookups. Every change is saved; on a failed save the
 * previous state is restored.
 */
public class PassManager {

    private static final int SINGLE = 1;
    private static final int PACK = 10;

    private final PassFileRepository repository;
    private final TicketManager tickets;
    private final Map<String, Pass> passes = new LinkedHashMap<>();

    public PassManager(PassFileRepository repository, TicketManager tickets) throws IOException {
        this.repository = repository;
        this.tickets = tickets;
        for (Pass p : repository.load(id -> tickets.findIndividualTicket(id).orElse(null))) {
            passes.put(p.getId(), p);
        }
    }

    public Collection<Pass> getPasses() {
        return Collections.unmodifiableCollection(passes.values());
    }

    public Optional<Pass> find(String id) {
        return Optional.ofNullable(passes.get(id));
    }

    /** Buys 1 or 10 SpeedPasses with ids {@code purchaseId-1..n}; returns the total charged. */
    public BigDecimal buySpeedPasses(String purchaseId, String ticketId, String quantityText, LocalDate today)
            throws UserException {
        int quantity = parseQuantity(quantityText);
        if (purchaseId == null || purchaseId.isBlank()) {
            throw new UserException("The purchase id must not be empty");
        }
        IndividualTicket ticket = requireValidTicket(ticketId, today);
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String id = purchaseId + "-" + i;
            if (passes.containsKey(id)) {
                throw new UserException("A pass with id " + id + " already exists");
            }
            ids.add(id);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (String id : ids) {
            SpeedPass pass = new SpeedPass(id, ticket);
            passes.put(id, pass);
            total = total.add(pass.getPrice());
        }
        save(() -> ids.forEach(passes::remove));
        return total;
    }

    /** Buys the only Special Pass of a ticket; returns the price charged. */
    public BigDecimal buySpecialPass(String passId, String ticketId, LocalDate today) throws UserException {
        if (passId == null || passId.isBlank()) {
            throw new UserException("The pass id must not be empty");
        }
        if (passes.containsKey(passId)) {
            throw new UserException("A pass with id " + passId + " already exists");
        }
        IndividualTicket ticket = requireValidTicket(ticketId, today);
        if (passesOf(ticket).stream().anyMatch(SpecialPass.class::isInstance)) {
            throw new UserException("Ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(passId, ticket);
        passes.put(passId, pass);
        save(() -> passes.remove(passId));
        return pass.getPrice();
    }

    /** Refunds an unconsumed pass of the given kind whose ticket is dated later than {@code today}. */
    public void refund(String passId, Class<? extends Pass> kind, LocalDate today) throws UserException {
        Pass pass = passes.get(passId);
        if (pass == null || !kind.isInstance(pass)) {
            throw new UserException("No " + kindName(kind) + " with id " + passId);
        }
        if (pass.isConsumed()) {
            throw new UserException("Pass " + passId + " has been consumed and cannot be refunded");
        }
        if (!pass.getTicket().getDate().isAfter(today)) {
            throw new UserException("Pass " + passId + " belongs to a ticket for " + pass.getTicket().getDate()
                    + " and can only be refunded before that date");
        }
        Map<String, Pass> previous = new LinkedHashMap<>(passes);
        passes.remove(passId);
        save(() -> {
            passes.clear();
            passes.putAll(previous);
        });
    }

    /** The first unconsumed SpeedPass of the ticket in purchase order. */
    public Optional<Pass> firstUnusedSpeedPass(IndividualTicket ticket) {
        return passesOf(ticket).stream().filter(p -> p instanceof SpeedPass && !p.isConsumed()).findFirst();
    }

    /** The ticket's Special Pass if it exists and has not been consumed. */
    public Optional<Pass> unusedSpecialPass(IndividualTicket ticket) {
        return passesOf(ticket).stream().filter(p -> p instanceof SpecialPass && !p.isConsumed()).findFirst();
    }

    /** Marks all given passes consumed and saves them; on failure none stays consumed. */
    public void consume(List<Pass> toConsume) throws UserException {
        toConsume.forEach(Pass::markConsumed);
        try {
            repository.save(passes.values());
        } catch (IOException e) {
            toConsume.forEach(Pass::markUnconsumed);
            throw new UserException("Could not save passes: " + e.getMessage());
        }
    }

    /**
     * Removes the given passes and saves; returns a snapshot to give to
     * {@link #restore} if a later step of the same operation fails.
     */
    public Map<String, Pass> removeAll(List<Pass> toRemove) throws UserException {
        Map<String, Pass> snapshot = new LinkedHashMap<>(passes);
        toRemove.forEach(p -> passes.remove(p.getId()));
        save(() -> {
            passes.clear();
            passes.putAll(snapshot);
        });
        return snapshot;
    }

    /** Puts back the passes of a snapshot taken by {@link #removeAll}, in their original order, and saves. */
    public void restore(Map<String, Pass> snapshot) throws UserException {
        Map<String, Pass> current = new LinkedHashMap<>(passes);
        passes.clear();
        passes.putAll(snapshot);
        save(() -> {
            passes.clear();
            passes.putAll(current);
        });
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        return passes.values().stream().filter(p -> p.getTicket() == ticket).toList();
    }

    public boolean hasPasses(IndividualTicket ticket) {
        return passes.values().stream().anyMatch(p -> p.getTicket() == ticket);
    }

    /** Passes on any individual ticket of the user, including tickets generated by group tickets. */
    public List<Pass> passesOfUser(User user) {
        return passes.values().stream().filter(p -> p.getTicket().belongsTo(user)).toList();
    }

    private IndividualTicket requireValidTicket(String ticketId, LocalDate today) throws UserException {
        IndividualTicket ticket = tickets.findIndividualTicket(ticketId)
                .orElseThrow(() -> new UserException("No individual ticket with id " + ticketId));
        if (!ticket.isOnOrAfter(today)) {
            throw new UserException("Ticket " + ticketId + " is for " + ticket.getDate() + ", before " + today);
        }
        return ticket;
    }

    private static int parseQuantity(String text) throws UserException {
        if (String.valueOf(SINGLE).equals(text)) {
            return SINGLE;
        }
        if (String.valueOf(PACK).equals(text)) {
            return PACK;
        }
        throw new UserException("Invalid quantity '" + text + "': SpeedPasses are sold in quantities of 1 or 10");
    }

    private static String kindName(Class<? extends Pass> kind) {
        return kind == SpecialPass.class ? "Special Pass" : "SpeedPass";
    }

    private void save(Runnable rollback) throws UserException {
        try {
            repository.save(passes.values());
        } catch (IOException e) {
            rollback.run();
            throw new UserException("Could not save passes: " + e.getMessage());
        }
    }
}
