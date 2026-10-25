package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.PassType;
import es.upm.etsisi.park.business.model.SpecialPass;
import es.upm.etsisi.park.business.model.SpeedPass;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.validation.UserValidator;
import es.upm.etsisi.park.persistence.PassRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Singleton managing SpeedPasses and Special Passes (their own identifier space).
 */
public final class PassController {

    public static final String SEPARATOR = ":";
    private static final List<Integer> SPEEDPASS_QUANTITIES = List.of(1, 10);

    private static PassController instance;

    private PassRepository repository;
    private SystemController system;
    private TicketController tickets;
    private final Map<String, Pass> passes = new LinkedHashMap<>();

    private PassController() {
    }

    public static synchronized PassController getInstance() {
        if (instance == null) {
            instance = new PassController();
        }
        return instance;
    }

    /**
     * Loads passes and attaches each one to its individual ticket.
     */
    public void initialize(PassRepository passRepository, SystemController systemController,
                           TicketController ticketController) {
        this.repository = passRepository;
        this.system = systemController;
        this.tickets = ticketController;
        passes.clear();
        for (Pass pass : passRepository.loadAll(ticketController::findIndividual)) {
            passes.put(pass.getId(), pass);
            attach(pass);
        }
    }

    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantityText) {
        requireIdentifier(purchaseId);
        int quantity = parseQuantity(quantityText);
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String id = purchaseId + SEPARATOR + i;
            if (passes.containsKey(id)) {
                throw new DuplicateException("Pass identifier " + id + " is already in use");
            }
            ids.add(id);
        }
        List<SpeedPass> created = new ArrayList<>();
        for (String id : ids) {
            SpeedPass pass = new SpeedPass(id, ticket);
            passes.put(id, pass);
            ticket.addSpeedPass(pass);
            created.add(pass);
        }
        persist();
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) {
        requireIdentifier(id);
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        if (passes.containsKey(id)) {
            throw new DuplicateException("Pass identifier " + id + " is already in use");
        }
        ticket.getSpecialPass().ifPresent(existing -> {
            throw new OperationNotAllowedException("Ticket " + ticketId + " already has Special Pass "
                    + existing.getId());
        });
        SpecialPass pass = new SpecialPass(id, ticket);
        ticket.setSpecialPass(pass);
        passes.put(id, pass);
        persist();
        return pass;
    }

    public Pass refund(String id, PassType expectedType) {
        Pass pass = passes.get(id);
        if (pass == null || pass.getType() != expectedType) {
            throw new NotFoundException(label(expectedType) + " " + id + " does not exist");
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException(label(expectedType) + " " + id + " has been consumed");
        }
        if (!pass.isRefundableOn(system.getCurrentDate())) {
            throw new OperationNotAllowedException(label(expectedType) + " " + id + " belongs to ticket "
                    + pass.getTicket().getId() + ", which is for the current day or earlier");
        }
        pass.getTicket().removePass(pass);
        passes.remove(id);
        persist();
        return pass;
    }

    /**
     * Deletes every pass of the given tickets (cascade when tickets are deleted).
     */
    public void removeAllFor(Collection<IndividualTicket> ticketsToClear) {
        for (IndividualTicket ticket : ticketsToClear) {
            for (Pass pass : ticket.getPasses()) {
                ticket.removePass(pass);
                passes.remove(pass.getId());
            }
        }
        persist();
    }

    public Optional<Pass> find(String id) {
        return Optional.ofNullable(passes.get(id));
    }

    public void persist() {
        repository.saveAll(passes.values());
    }

    private IndividualTicket requirePurchasableTicket(String ticketId) {
        Ticket ticket = tickets.require(ticketId);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ValidationException("Ticket " + ticketId + " is not an individual ticket");
        }
        if (!ticket.isActiveOn(system.getCurrentDate())) {
            throw new OperationNotAllowedException("Ticket " + ticketId + " is dated before the current system date");
        }
        return individual;
    }

    private static void attach(Pass pass) {
        if (pass instanceof SpeedPass speed) {
            pass.getTicket().addSpeedPass(speed);
        } else if (pass instanceof SpecialPass special) {
            pass.getTicket().setSpecialPass(special);
        }
    }

    private static int parseQuantity(String text) {
        try {
            int quantity = Integer.parseInt(text.trim());
            if (SPEEDPASS_QUANTITIES.contains(quantity)) {
                return quantity;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new ValidationException("Invalid SpeedPass quantity: " + text + ". It must be 1 or 10");
    }

    private static String label(PassType type) {
        return type == PassType.SPEED ? "SpeedPass" : "Special Pass";
    }

    private static void requireIdentifier(String id) {
        if (!UserValidator.isNotBlank(id)) {
            throw new ValidationException("Pass identifier must not be empty");
        }
    }
}
