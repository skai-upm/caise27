package es.upm.etsisi.park.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.persistence.PassRepository;

/**
 * Manages SpeedPasses and Special Passes: purchases, refunds and the pass
 * identifier space. Passes are linked to their individual ticket. Singleton.
 */
public final class PassController {

    public static final List<Integer> SPEEDPASS_QUANTITIES = List.of(1, 10);

    private static final PassController INSTANCE = new PassController();

    private PassRepository repository;
    private SystemController system;
    private TicketController tickets;
    private final List<Pass> passes = new ArrayList<>();
    private final Map<String, Pass> index = new HashMap<>();

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(PassRepository repository, SystemController system, TicketController tickets) {
        this.repository = repository;
        this.system = system;
        this.tickets = tickets;
        passes.clear();
        index.clear();
    }

    /** Loads the passes and links each one to its ticket; must run after tickets are loaded. */
    public void load() throws PersistenceException {
        passes.clear();
        index.clear();
        try {
            for (Pass pass : repository.load(tickets.individualTicketsById())) {
                if (index.containsKey(Checks.key(pass.getId()))
                        || (pass instanceof SpecialPass && pass.getTicket().hasSpecialPass())) {
                    continue;
                }
                pass.getTicket().attach(pass);
                register(pass);
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not load passes: " + e.getMessage(), e);
        }
    }

    public List<Pass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public Pass find(String id) {
        return index.get(Checks.key(id));
    }

    public boolean isIdTaken(String id) {
        return index.containsKey(Checks.key(id));
    }

    /** Buys 1 or 10 SpeedPasses identified {@code purchaseId:n}; all identifiers are checked first. */
    public List<SpeedPass> createSpeedPasses(String purchaseId, String ticketId, String quantityText)
            throws ParkException {
        Checks.requireText(purchaseId, "Purchase identifier");
        int quantity = parseQuantity(quantityText);
        IndividualTicket ticket = requireUsableTicket(ticketId);
        List<SpeedPass> created = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = SpeedPass.passId(purchaseId, n);
            requireFreeId(id);
            created.add(SpeedPass.purchase(id, ticket));
        }
        for (SpeedPass pass : created) {
            ticket.attach(pass);
            register(pass);
        }
        persist();
        return created;
    }

    /** Buys the single Special Pass a ticket may have. */
    public SpecialPass createSpecialPass(String id, String ticketId) throws ParkException {
        Checks.requireText(id, "Pass identifier");
        IndividualTicket ticket = requireUsableTicket(ticketId);
        requireFreeId(id);
        if (ticket.hasSpecialPass()) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " already has Special Pass "
                    + ticket.getSpecialPass().getId());
        }
        SpecialPass pass = SpecialPass.purchase(id, ticket);
        ticket.attach(pass);
        register(pass);
        persist();
        return pass;
    }

    public void deleteSpeedPass(String id) throws ParkException {
        refund(id, SpeedPass.class);
    }

    public void deleteSpecialPass(String id) throws ParkException {
        refund(id, SpecialPass.class);
    }

    /** Removes passes whose ticket is being deleted; the caller persists afterwards. */
    void discard(Collection<Pass> removed) {
        for (Pass pass : removed) {
            pass.getTicket().detach(pass);
            passes.remove(pass);
            index.remove(Checks.key(pass.getId()));
        }
    }

    public void persist() throws PersistenceException {
        try {
            repository.save(passes);
        } catch (IOException e) {
            throw new PersistenceException("Could not save passes: " + e.getMessage(), e);
        }
    }

    private void refund(String id, Class<? extends Pass> type) throws ParkException {
        Pass pass = find(id);
        if (pass == null || !type.isInstance(pass)) {
            throw new NotFoundException((type == SpeedPass.class ? "SpeedPass " : "Special Pass ") + id + " not found");
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException(pass.getKind() + " " + pass.getId() + " is consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(system.getDate())) {
            throw new OperationNotAllowedException(pass.getKind() + " " + pass.getId() + " belongs to ticket "
                    + pass.getTicket().getId() + " dated " + pass.getTicket().getDate()
                    + "; only passes of tickets after " + system.getDate() + " can be refunded");
        }
        discard(List.of(pass));
        persist();
    }

    private IndividualTicket requireUsableTicket(String ticketId) throws ParkException {
        IndividualTicket ticket = tickets.requireIndividual(ticketId);
        if (ticket.getDate().isBefore(system.getDate())) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + ", before the current date " + system.getDate());
        }
        return ticket;
    }

    private static int parseQuantity(String text) throws ValidationException {
        for (int quantity : SPEEDPASS_QUANTITIES) {
            if (String.valueOf(quantity).equals(text)) {
                return quantity;
            }
        }
        throw new ValidationException("Invalid quantity '" + text + "': SpeedPasses are sold in quantities of 1 or 10");
    }

    private void requireFreeId(String id) throws DuplicateException {
        if (isIdTaken(id)) {
            throw new DuplicateException("Pass identifier " + id + " is already in use");
        }
    }

    private void register(Pass pass) {
        passes.add(pass);
        index.put(Checks.key(pass.getId()), pass);
    }
}
