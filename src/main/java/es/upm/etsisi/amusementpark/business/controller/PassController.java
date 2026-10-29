package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ConflictException;
import es.upm.etsisi.amusementpark.business.exception.ForbiddenOperationException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.PassLookup;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Single manager of SpeedPasses and Special Passes: purchase, refund and lookup. Tickets and the
 * current date are supplied by the park controller.
 */
public final class PassController implements PassLookup {

    private static final PassController INSTANCE = new PassController();
    private static final String ID_SEPARATOR = ":";

    private PassRepository repository;
    private final List<Pass> passes = new ArrayList<>();

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository; passes are loaded with {@link #load} once tickets exist.
     */
    public void configure(PassRepository repository) {
        this.repository = repository;
        passes.clear();
    }

    public void load(Function<String, IndividualTicket> ticketById) {
        passes.clear();
        passes.addAll(repository.load(ticketById));
    }

    public void save() {
        repository.save(passes);
    }

    public List<Pass> getPasses() {
        return List.copyOf(passes);
    }

    public Pass findPass(String id) {
        for (Pass pass : passes) {
            if (pass.getId().equals(id)) {
                return pass;
            }
        }
        return null;
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }

    @Override
    public SpeedPass firstUnconsumedSpeedPass(IndividualTicket ticket) {
        for (Pass pass : passes) {
            if (pass instanceof SpeedPass speed && pass.getTicket() == ticket && !pass.isConsumed()) {
                return speed;
            }
        }
        return null;
    }

    @Override
    public SpecialPass specialPassOf(IndividualTicket ticket) {
        for (Pass pass : passes) {
            if (pass instanceof SpecialPass special && pass.getTicket() == ticket) {
                return special;
            }
        }
        return null;
    }

    private static void requireActive(IndividualTicket ticket, LocalDate today) throws ForbiddenOperationException {
        if (!ticket.isActive(today)) {
            throw new ForbiddenOperationException("ticket " + ticket.getId() + " is dated before " + today);
        }
    }

    private void requireFreeId(String id) throws ParkException {
        if (id.isBlank()) {
            throw new ValidationException("identifier must not be empty");
        }
        if (findPass(id) != null) {
            throw new ConflictException("pass identifier already in use: " + id);
        }
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:<n>}; all or none are created.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, IndividualTicket ticket, String quantityText,
                                          LocalDate today) throws ParkException {
        int quantity;
        switch (quantityText.strip()) {
            case "1" -> quantity = 1;
            case "10" -> quantity = 10;
            default -> throw new ValidationException("quantity must be 1 or 10: " + quantityText);
        }
        if (purchaseId.isBlank()) {
            throw new ValidationException("identifier must not be empty");
        }
        requireActive(ticket, today);
        List<SpeedPass> bought = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = purchaseId + ID_SEPARATOR + n;
            requireFreeId(id);
            bought.add(SpeedPass.purchase(id, ticket));
        }
        passes.addAll(bought);
        save();
        return bought;
    }

    public SpecialPass buySpecialPass(String id, IndividualTicket ticket, LocalDate today) throws ParkException {
        requireFreeId(id);
        requireActive(ticket, today);
        SpecialPass existing = specialPassOf(ticket);
        if (existing != null) {
            throw new ConflictException("ticket " + ticket.getId() + " already has Special Pass " + existing.getId());
        }
        SpecialPass pass = SpecialPass.purchase(id, ticket);
        passes.add(pass);
        save();
        return pass;
    }

    /**
     * Refunds an unconsumed pass of the given type whose ticket is dated after today.
     */
    public void refund(String id, Class<? extends Pass> type, LocalDate today) throws ParkException {
        Pass pass = findPass(id);
        if (!type.isInstance(pass)) {
            throw new NotFoundException(type.getSimpleName() + " not found: " + id);
        }
        if (pass.isConsumed()) {
            throw new ForbiddenOperationException("pass " + id + " is consumed and cannot be refunded");
        }
        if (!pass.getTicket().getDate().isAfter(today)) {
            throw new ForbiddenOperationException("only passes of tickets dated after " + today + " can be refunded");
        }
        passes.remove(pass);
        save();
    }

    /**
     * Removes every pass of the given tickets (used when the tickets themselves are deleted).
     */
    public void removePassesOf(Collection<IndividualTicket> tickets) {
        passes.removeIf(pass -> tickets.contains(pass.getTicket()));
    }
}
