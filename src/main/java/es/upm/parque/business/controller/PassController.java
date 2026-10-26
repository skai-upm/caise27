package es.upm.parque.business.controller;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.persistence.PassRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for SpeedPasses and Special Passes attached to individual tickets.
 */
public class PassController {

    private static final PassController INSTANCE = new PassController();
    private static final String SPEED_PREFIX = "-SP";
    private static final String SPECIAL_SUFFIX = "-SPECIAL";

    private PassRepository repository;
    private TicketController ticketController;
    private SystemDate systemDate;
    private final Map<String, Pass> passes = new LinkedHashMap<>();

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators of the controller and (re)loads its data. Called during application
     * initialization.
     */
    public PassController configure(PassRepository repository, TicketController ticketController,
                                    SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.ticketController = ticketController;
        this.systemDate = systemDate;
        passes.clear();
        for (Pass pass : repository.loadAll(this::individualTicket)) {
            passes.put(pass.getId(), pass);
        }
        return this;
    }

    public Pass find(String id) {
        requireConfigured();
        return passes.get(id);
    }

    public List<Pass> getPasses() {
        requireConfigured();
        return new ArrayList<>(passes.values());
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        requireConfigured();
        return passes.values().stream().filter(pass -> pass.getTicket() == ticket).toList();
    }

    /**
     * True if the ticket, or any ticket composing it, has passes.
     */
    public boolean hasPasses(Ticket ticket) {
        requireConfigured();
        if (ticket instanceof GroupTicket groupTicket) {
            return groupTicket.getTickets().stream().anyMatch(internal -> !passesOf(internal).isEmpty());
        }
        return ticket instanceof IndividualTicket individual && !passesOf(individual).isEmpty();
    }

    /**
     * Buys 1 or 10 SpeedPasses for the ticket and returns them.
     */
    public List<SpeedPass> buySpeedPasses(String ticketId, String quantityText) {
        requireConfigured();
        int quantity = switch (quantityText.trim()) {
            case "1" -> 1;
            case "10" -> 10;
            default -> throw new BusinessException("Invalid SpeedPass quantity: " + quantityText + " (1 or 10)");
        };
        IndividualTicket ticket = requireEligibleTicket(ticketId);
        int next = nextSpeedPassIndex(ticket);
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SpeedPass pass = new SpeedPass(ticket.getId() + SPEED_PREFIX + (next + i), ticket);
            passes.put(pass.getId(), pass);
            bought.add(pass);
        }
        save();
        return bought;
    }

    public SpecialPass buySpecialPass(String ticketId) {
        requireConfigured();
        IndividualTicket ticket = requireEligibleTicket(ticketId);
        String id = ticket.getId() + SPECIAL_SUFFIX;
        if (passes.containsKey(id)) {
            throw new BusinessException("Ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.put(id, pass);
        save();
        return pass;
    }

    /**
     * Refunds an unused SpeedPass before its ticket's date and returns it.
     */
    public Pass refundSpeedPass(String passId) {
        requireConfigured();
        Pass pass = passes.get(passId);
        if (!(pass instanceof SpeedPass)) {
            throw new BusinessException("No SpeedPass with identifier " + passId);
        }
        return refund(pass);
    }

    /**
     * Refunds the unused Special Pass of the ticket before its date and returns it.
     */
    public Pass refundSpecialPass(String ticketId) {
        requireConfigured();
        Pass pass = passes.get(ticketId + SPECIAL_SUFFIX);
        if (pass == null) {
            throw new BusinessException("Ticket " + ticketId + " has no Special Pass");
        }
        return refund(pass);
    }

    /**
     * The first unused SpeedPass of the ticket in purchase order, or {@code null} if it has none.
     */
    public Pass firstUnusedSpeedPass(IndividualTicket ticket) {
        requireConfigured();
        return passesOf(ticket).stream()
                .filter(pass -> pass instanceof SpeedPass && !pass.isConsumed())
                .findFirst()
                .orElse(null);
    }

    /**
     * The ticket's Special Pass, or {@code null} if it has none.
     */
    public Pass specialPassOf(IndividualTicket ticket) {
        requireConfigured();
        return passes.get(ticket.getId() + SPECIAL_SUFFIX);
    }

    /**
     * Marks the passes as consumed and saves the change.
     */
    public void consume(List<Pass> used) {
        requireConfigured();
        used.forEach(Pass::consume);
        if (!used.isEmpty()) {
            save();
        }
    }

    public static BigDecimal total(List<? extends Pass> bought) {
        return bought.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Pass refund(Pass pass) {
        if (pass.isConsumed()) {
            throw new BusinessException("Pass " + pass.getId() + " has been consumed and cannot be refunded");
        }
        if (!pass.getTicket().isFuture(systemDate.get())) {
            throw new BusinessException("Pass " + pass.getId() + " can only be refunded before the ticket date");
        }
        passes.remove(pass.getId());
        save();
        return pass;
    }

    private IndividualTicket requireEligibleTicket(String ticketId) {
        IndividualTicket ticket = individualTicket(ticketId);
        if (ticket == null) {
            throw new BusinessException("No individual ticket with identifier " + ticketId);
        }
        if (!ticket.isCurrentOrFuture(systemDate.get())) {
            throw new BusinessException("Ticket " + ticketId + " is in the past");
        }
        return ticket;
    }

    private IndividualTicket individualTicket(String ticketId) {
        return ticketController.find(ticketId) instanceof IndividualTicket individual ? individual : null;
    }

    private int nextSpeedPassIndex(IndividualTicket ticket) {
        String prefix = ticket.getId() + SPEED_PREFIX;
        int max = 0;
        for (String id : passes.keySet()) {
            if (id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException e) {
                    // not a SpeedPass of this ticket
                }
            }
        }
        return max + 1;
    }

    private void save() {
        try {
            repository.saveAll(passes.values());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save passes", e);
        }
    }

    private void requireConfigured() {
        if (repository == null) {
            throw new IllegalStateException("PassController is not configured");
        }
    }
}
