package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.PassLookup;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.pass.SpeedPass;
import es.upm.amusementpark.business.model.ticket.IndividualAccess;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;
import es.upm.amusementpark.business.repository.PassRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages SpeedPasses and Special Passes: purchase, refund, lookup and deletion with their tickets.
 */
public final class PassController implements PassLookup {

    private static final PassController INSTANCE = new PassController();
    private static final String SINGLE = "1";
    private static final String PACK = "10";

    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private PassRepository repository;
    private SystemController system;
    private TicketController tickets;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(PassRepository repository) {
        this.repository = repository;
    }

    public void setCollaborators(SystemController system, TicketController tickets) {
        this.system = system;
        this.tickets = tickets;
    }

    public void load() throws PersistenceException {
        passes.clear();
        try {
            for (Pass pass : repository.load(tickets.ticketsById())) {
                passes.put(pass.getId(), pass);
            }
        } catch (IOException e) {
            throw new PersistenceException("could not load passes: " + e.getMessage(), e);
        }
    }

    public void save() throws PersistenceException {
        try {
            repository.saveAll(passes.values());
        } catch (IOException e) {
            throw new PersistenceException("could not save passes: " + e.getMessage(), e);
        }
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}; nothing is created on any conflict.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) throws ParkException {
        if (purchaseId.isEmpty()) {
            throw new ValidationException("purchase identifier must not be empty");
        }
        if (!quantity.equals(SINGLE) && !quantity.equals(PACK)) {
            throw new ValidationException("quantity must be 1 or 10: " + quantity);
        }
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        int count = Integer.parseInt(quantity);
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String id = purchaseId + ":" + i;
            requireFreeId(id);
            ids.add(id);
        }
        BigDecimal unitPrice = SpeedPass.priceFor(ticket);
        List<SpeedPass> created = new ArrayList<>();
        for (String id : ids) {
            SpeedPass pass = new SpeedPass(id, ticket, unitPrice, false);
            passes.put(id, pass);
            created.add(pass);
        }
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        if (id.isEmpty()) {
            throw new ValidationException("pass identifier must not be empty");
        }
        requireFreeId(id);
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        if (specialPassOf(ticket).isPresent()) {
            throw new DuplicateException("ticket already has a Special Pass: " + ticketId);
        }
        SpecialPass pass = new SpecialPass(id, ticket, SpecialPass.priceFor(ticket), false);
        passes.put(id, pass);
        return pass;
    }

    public void refundSpeedPass(String id) throws ParkException {
        refund(id, SpeedPass.class);
    }

    public void refundSpecialPass(String id) throws ParkException {
        refund(id, SpecialPass.class);
    }

    /**
     * Removes every pass of the given tickets (used when those tickets are validly deleted).
     */
    public void removePassesOf(Collection<IndividualTicket> deletedTickets) {
        passes.values().removeIf(pass -> deletedTickets.contains(pass.getTicket()));
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes.values()) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }

    @Override
    public Optional<SpeedPass> availableSpeedPass(IndividualAccess ticket) {
        for (Pass pass : passes.values()) {
            if (pass instanceof SpeedPass speedPass && pass.getTicket() == ticket && !pass.isConsumed()) {
                return Optional.of(speedPass);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<SpecialPass> specialPassOf(IndividualAccess ticket) {
        for (Pass pass : passes.values()) {
            if (pass instanceof SpecialPass specialPass && pass.getTicket() == ticket) {
                return Optional.of(specialPass);
            }
        }
        return Optional.empty();
    }

    public Pass findPass(String id) {
        return passes.get(id);
    }

    public List<Pass> getPasses() {
        return new ArrayList<>(passes.values());
    }

    private void refund(String id, Class<? extends Pass> kind) throws ParkException {
        Pass pass = passes.get(id);
        if (pass == null || !kind.isInstance(pass)) {
            throw new NotFoundException(kind.getSimpleName() + " not found: " + id);
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("pass already consumed: " + id);
        }
        if (!pass.isRefundable(system.getCurrentDate())) {
            throw new OperationNotAllowedException("only passes of tickets dated after today can be refunded: " + id);
        }
        passes.remove(id);
    }

    private IndividualTicket requirePurchasableTicket(String ticketId) throws ParkException {
        IndividualTicket ticket = tickets.requireIndividualTicket(ticketId);
        if (!ticket.isActive(system.getCurrentDate())) {
            throw new OperationNotAllowedException("ticket date is in the past: " + ticketId);
        }
        return ticket;
    }

    private void requireFreeId(String id) throws DuplicateException {
        if (passes.containsKey(id)) {
            throw new DuplicateException("pass identifier already exists: " + id);
        }
    }
}
