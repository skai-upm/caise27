package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassType;
import es.upm.amusementpark.business.model.PassWallet;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TransactionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages SpeedPasses and Special Passes. Passes reference their individual
 * ticket; the passes of a ticket are found by traversing the passes.
 */
public final class PassController extends AbstractController implements PassWallet {

    private static final PassController INSTANCE = new PassController();

    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private PassRepository repository;
    private SystemController system;
    private TicketController tickets;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and the collaborating controllers. */
    public void configure(PassRepository repository, TransactionManager transactions, SystemController system,
                          TicketController tickets) {
        this.repository = repository;
        setTransactions(transactions);
        this.system = system;
        this.tickets = tickets;
        passes.clear();
    }

    void load() throws IOException {
        passes.clear();
        for (Pass pass : repository.loadAll(id -> tickets.find(id) instanceof IndividualTicket t ? t : null)) {
            passes.put(pass.getId(), pass);
        }
    }

    void save() throws IOException {
        repository.saveAll(passes.values());
    }

    public Pass find(String id) {
        return passes.get(id);
    }

    public List<Pass> getPasses() {
        return new ArrayList<>(passes.values());
    }

    /** Every pass of {@code ticket}, consumed or not, in purchase order. */
    public List<Pass> passesOf(IndividualAccess ticket) {
        List<Pass> owned = new ArrayList<>();
        for (Pass pass : passes.values()) {
            if (pass.getTicket() == ticket) {
                owned.add(pass);
            }
        }
        return owned;
    }

    /**
     * Buys {@code quantity} (1 or 10) SpeedPasses for an individual ticket
     * dated today or later, identified {@code <purchaseId>:1} to
     * {@code <purchaseId>:<quantity>}. Nothing is created if any identifier is
     * already used.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantityText)
            throws ParkException {
        requireText(purchaseId, "Purchase identifier");
        int quantity = parseQuantity(quantityText);
        IndividualTicket ticket = requireUsableTicket(ticketId);
        List<SpeedPass> bought = new ArrayList<>();
        for (int number = 1; number <= quantity; number++) {
            String id = purchaseId + ":" + number;
            requireFreeId(id);
            bought.add(new SpeedPass(id, ticket));
        }
        bought.forEach(pass -> passes.put(pass.getId(), pass));
        persist(this::save, () -> bought.forEach(pass -> passes.remove(pass.getId())));
        return bought;
    }

    /** Buys the only Special Pass an individual ticket dated today or later can ever have. */
    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        requireText(id, "Special Pass identifier");
        IndividualTicket ticket = requireUsableTicket(ticketId);
        if (specialPassOf(ticket) != null) {
            throw new OperationNotAllowedException("Ticket " + ticketId + " already has a Special Pass");
        }
        requireFreeId(id);
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.put(id, pass);
        persist(this::save, () -> passes.remove(id));
        return pass;
    }

    /** Refunds an unconsumed pass of {@code type} whose ticket is dated after today. */
    public void refund(String id, PassType type) throws ParkException {
        Pass pass = passes.get(id);
        if (pass == null || pass.getType() != type) {
            throw new NotFoundException("No " + (type == PassType.SPEED ? "SpeedPass" : "Special Pass")
                    + " with identifier " + id);
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Pass " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(system.getDate())) {
            throw new OperationNotAllowedException("Pass " + id + " belongs to ticket " + pass.getTicket().getId()
                    + " dated " + pass.getTicket().getDate() + "; only passes of tickets after "
                    + system.getDate() + " can be refunded");
        }
        passes.remove(id);
        persist(this::save, () -> passes.put(id, pass));
    }

    @Override
    public SpeedPass availableSpeedPass(IndividualAccess ticket) {
        for (Pass pass : passesOf(ticket)) {
            if (pass instanceof SpeedPass speedPass && !pass.isConsumed()) {
                return speedPass;
            }
        }
        return null;
    }

    @Override
    public SpecialPass unconsumedSpecialPass(IndividualAccess ticket) {
        SpecialPass pass = specialPassOf(ticket);
        return pass == null || pass.isConsumed() ? null : pass;
    }

    /** The Special Pass of {@code ticket}, consumed or not, or {@code null}. */
    public SpecialPass specialPassOf(IndividualAccess ticket) {
        for (Pass pass : passesOf(ticket)) {
            if (pass instanceof SpecialPass specialPass) {
                return specialPass;
            }
        }
        return null;
    }

    /** Removes every pass of {@code owners}; the returned passes can be given to {@link #restore}. */
    List<Pass> removeFor(Collection<IndividualTicket> owners) {
        List<Pass> removed = new ArrayList<>();
        for (Pass pass : passes.values()) {
            if (owners.contains(pass.getTicket())) {
                removed.add(pass);
            }
        }
        removed.forEach(pass -> passes.remove(pass.getId()));
        return removed;
    }

    void restore(List<Pass> removed) {
        removed.forEach(pass -> passes.put(pass.getId(), pass));
    }

    private IndividualTicket requireUsableTicket(String ticketId) throws ParkException {
        Ticket ticket = tickets.require(ticketId);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new OperationNotAllowedException("Passes are bought for individual tickets; " + ticketId
                    + " is a group ticket (use one of its individual tickets)");
        }
        if (!individual.isActiveOn(system.getDate())) {
            throw new OperationNotAllowedException("Ticket " + ticketId + " is dated " + individual.getDate()
                    + "; passes can only be bought for tickets dated " + system.getDate() + " or later");
        }
        return individual;
    }

    private void requireFreeId(String id) throws OperationNotAllowedException {
        if (passes.containsKey(id)) {
            throw new OperationNotAllowedException("A pass with identifier " + id + " already exists");
        }
    }

    private static int parseQuantity(String text) throws InvalidDataException {
        if ("1".equals(text)) {
            return 1;
        }
        if ("10".equals(text)) {
            return 10;
        }
        throw new InvalidDataException("Invalid quantity '" + text + "' (SpeedPasses are sold in 1 or 10)");
    }
}
