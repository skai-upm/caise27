package com.parque.business.service;

import com.parque.business.model.Admission;
import com.parque.business.model.GroupAccess;
import com.parque.business.model.IndividualTicket;
import com.parque.business.model.Pass;
import com.parque.business.model.SpecialPass;
import com.parque.business.model.SpeedPass;
import com.parque.business.model.User;
import com.parque.persistence.PassRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Purchase, refund and lookup of passes attached to individual tickets. The system date is supplied by the
 * controller. Rejected operations throw {@link IllegalArgumentException} and change nothing.
 */
public class PassService {
    private final PassRepository repository;
    private final TicketService tickets;
    private List<Pass> passes;

    public PassService(PassRepository repository, TicketService tickets) throws IOException {
        this.repository = repository;
        this.tickets = tickets;
        this.passes = new ArrayList<>(repository.load(tickets::find));
    }

    public List<Pass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public Pass find(String id) {
        for (Pass p : passes) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code compra-1..n}; all or nothing. Returns the created passes. */
    public List<Pass> buySpeedPasses(String purchaseId, String ticketId, String quantityText, LocalDate today) {
        require(UserValidator.isNonBlank(purchaseId), "Pass identifier must not be empty");
        require(quantityText.equals("1") || quantityText.equals("10"),
                "Invalid SpeedPass quantity: '" + quantityText + "' (allowed: 1 or 10)");
        IndividualTicket ticket = usableTicket(ticketId, today);
        int quantity = Integer.parseInt(quantityText);
        List<Pass> created = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String id = purchaseId + "-" + i;
            require(find(id) == null, "A pass with identifier " + id + " already exists");
            created.add(new SpeedPass(id, ticket));
        }
        List<Pass> next = new ArrayList<>(passes);
        next.addAll(created);
        commit(next);
        return created;
    }

    /** Buys the (only) Special Pass of a ticket. */
    public Pass buySpecialPass(String id, String ticketId, LocalDate today) {
        require(UserValidator.isNonBlank(id), "Pass identifier must not be empty");
        require(find(id) == null, "A pass with identifier " + id + " already exists");
        IndividualTicket ticket = usableTicket(ticketId, today);
        for (Pass p : passes) {
            require(!(p instanceof SpecialPass && p.getTicket() == ticket),
                    "Ticket " + ticketId + " already has a Special Pass");
        }
        Pass pass = new SpecialPass(id, ticket);
        List<Pass> next = new ArrayList<>(passes);
        next.add(pass);
        commit(next);
        return pass;
    }

    /** Refunds (removes) an unconsumed pass of the given kind whose ticket is dated after {@code today}. */
    public void refund(String id, Class<? extends Pass> kind, LocalDate today) {
        Pass pass = find(id);
        require(pass != null && kind.isInstance(pass), "No " + kindName(kind) + " with identifier " + id);
        require(!pass.isConsumed(), "Pass " + id + " has been consumed and cannot be refunded");
        require(pass.getTicket().isFuture(today), "Pass " + id + " cannot be refunded: its ticket date "
                + pass.getTicket().getDate() + " is not after " + today);
        List<Pass> next = new ArrayList<>(passes);
        next.remove(pass);
        commit(next);
    }

    /** Passes attached to the ticket. */
    public List<Pass> passesOf(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass p : passes) {
            if (p.getTicket() == ticket) {
                result.add(p);
            }
        }
        return result;
    }

    /** Passes attached to any ticket owned by the user (including group-ticket constituents). */
    public List<Pass> passesOfUser(User user) {
        List<Pass> result = new ArrayList<>();
        for (Pass p : passes) {
            if (p.getTicket().getUser() == user) {
                result.add(p);
            }
        }
        return result;
    }

    /** Removes (and saves) the passes of the given tickets; returns the previous pass list for {@link #restore}. */
    public List<Pass> removePassesOf(Collection<IndividualTicket> deleted) {
        List<Pass> previous = passes;
        List<Pass> next = new ArrayList<>(passes);
        boolean changed = next.removeIf(p -> deleted.contains(p.getTicket()));
        if (changed) {
            commit(next);
        }
        return previous;
    }

    /** Restores a pass list previously returned by {@link #removePassesOf} (compensation after a failure). */
    public void restore(List<Pass> previous) {
        commit(new ArrayList<>(previous));
    }

    /** The individual tickets behind an admission: itself, or the constituents of a group ticket. */
    public static List<IndividualTicket> individualTicketsOf(Admission ticket) {
        if (ticket instanceof GroupAccess group) {
            return group.getConstituents();
        }
        if (ticket instanceof IndividualTicket individual) {
            return List.of(individual);
        }
        return List.of();
    }

    /**
     * One unused SpeedPass (the earliest purchased) for every individual ticket behind {@code ticket}; rejects if
     * any of them has none. Does not change any pass.
     */
    public List<Pass> selectSpeedPasses(Admission ticket) {
        List<Pass> selected = new ArrayList<>();
        for (IndividualTicket t : individualTicketsOf(ticket)) {
            Pass found = null;
            for (Pass p : passes) {
                if (p instanceof SpeedPass && p.getTicket() == t && !p.isConsumed()) {
                    found = p;
                    break;
                }
            }
            require(found != null, "Ticket " + t.getId() + " has no unused SpeedPass");
            selected.add(found);
        }
        require(!selected.isEmpty(), "Ticket " + ticket.getId() + " has no individual tickets");
        return selected;
    }

    /** The unused Special Pass of the ticket; rejects if it has none or it was consumed. Does not change it. */
    public Pass selectSpecialPass(IndividualTicket ticket) {
        for (Pass p : passes) {
            if (p instanceof SpecialPass && p.getTicket() == ticket) {
                require(!p.isConsumed(), "Special Pass " + p.getId() + " of ticket " + ticket.getId()
                        + " has already been used");
                return p;
            }
        }
        throw new IllegalArgumentException("Ticket " + ticket.getId() + " has no Special Pass");
    }

    /** Marks the passes as consumed and saves; on a save failure the passes are restored and the error raised. */
    public void consume(List<Pass> selected) {
        for (Pass p : selected) {
            p.consume();
        }
        try {
            repository.save(passes);
        } catch (IOException e) {
            for (Pass p : selected) {
                p.restoreUnconsumed();
            }
            throw new IllegalStateException("Could not save passes: " + e.getMessage(), e);
        }
    }

    /** Persists the current state (e.g. after a pass is consumed). */
    public void save() {
        commit(passes);
    }

    private IndividualTicket usableTicket(String ticketId, LocalDate today) {
        IndividualTicket ticket = tickets.find(ticketId);
        require(ticket != null, "No individual ticket with identifier " + ticketId);
        require(ticket.isCurrentOrFuture(today),
                "Ticket " + ticketId + " is dated " + ticket.getDate() + ", before the system date " + today);
        return ticket;
    }

    private static String kindName(Class<? extends Pass> kind) {
        return kind == SpeedPass.class ? "SpeedPass" : "Special Pass";
    }

    private void commit(List<Pass> next) {
        try {
            repository.save(next);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save passes: " + e.getMessage(), e);
        }
        passes = next;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /** Sum of the prices of the given passes. */
    public static BigDecimal total(List<Pass> list) {
        BigDecimal sum = new BigDecimal("0.00");
        for (Pass p : list) {
            sum = sum.add(p.getPrice());
        }
        return sum;
    }
}
