package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.PassRegistry;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;
import es.upm.park.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Purchase, refund and lookup of SpeedPasses and Special Passes. Passes
 * share one identifier space, separate from tickets. Single instance.
 */
public final class PassController implements PassRegistry {

    private static final String PACKAGE_ID_SEPARATOR = ":";

    private Park park;

    private PassController() {
    }

    private static final class Holder {
        private static final PassController INSTANCE = new PassController();
    }

    public static PassController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    private Park park() {
        if (park == null) {
            throw new IllegalStateException("PassController has not been configured");
        }
        return park;
    }

    /**
     * Buys {@code quantity} (1 or 10) SpeedPasses identified
     * {@code <purchaseId>:1..quantity}; nothing is created on any conflict.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, int quantity) throws ParkException {
        if (quantity != 1 && quantity != 10) {
            throw new ValidationException("SpeedPasses are sold individually (1) or in packages of 10, not "
                    + quantity);
        }
        requireNotBlank(purchaseId);
        IndividualTicket ticket = purchasableTicket(ticketId);
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String id = purchaseId + PACKAGE_ID_SEPARATOR + i;
            requireFreeId(id);
            ids.add(id);
        }
        List<SpeedPass> created = new ArrayList<>();
        for (String id : ids) {
            SpeedPass pass = new SpeedPass(id, ticket);
            park().addPass(pass);
            created.add(pass);
        }
        park().save();
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        requireNotBlank(id);
        IndividualTicket ticket = purchasableTicket(ticketId);
        requireFreeId(id);
        Optional<SpecialPass> existing = specialPassOf(ticket.getId());
        if (existing.isPresent()) {
            throw new ConflictException("Ticket " + ticketId + " already has Special Pass " + existing.get().getId());
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        park().addPass(pass);
        park().save();
        return pass;
    }

    public void refundSpeedPass(String id) throws ParkException {
        refund(id, SpeedPass.class);
    }

    public void refundSpecialPass(String id) throws ParkException {
        refund(id, SpecialPass.class);
    }

    private void refund(String id, Class<? extends Pass> kind) throws ParkException {
        Pass pass = park().findPass(id);
        if (!kind.isInstance(pass)) {
            throw new NotFoundException("No " + kindName(kind) + " with identifier " + id);
        }
        if (pass.isConsumed()) {
            throw new BusinessRuleException("Pass " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.getTicket().isDeletableOn(park().getCurrentDate())) {
            throw new BusinessRuleException("Pass " + id + " belongs to ticket " + pass.getTicket().getId()
                    + ", which is for today or an earlier date; it cannot be refunded");
        }
        park().removePass(pass);
        park().save();
    }

    /**
     * Passes attached to the given individual tickets.
     */
    public List<Pass> passesOf(List<IndividualTicket> tickets) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : park().passes()) {
            if (tickets.contains(pass.getTicket())) {
                result.add(pass);
            }
        }
        return result;
    }

    /**
     * Removes every pass attached to the given tickets, as part of deleting
     * those tickets. The caller saves the park afterwards.
     */
    void removeAllFor(List<IndividualTicket> tickets) {
        for (Pass pass : passesOf(tickets)) {
            park().removePass(pass);
        }
    }

    @Override
    public List<SpeedPass> speedPassesOf(String individualTicketId) {
        List<SpeedPass> result = new ArrayList<>();
        for (Pass pass : park().passes()) {
            if (pass instanceof SpeedPass speedPass && pass.getTicket().getId().equals(individualTicketId)) {
                result.add(speedPass);
            }
        }
        return result;
    }

    @Override
    public Optional<SpecialPass> specialPassOf(String individualTicketId) {
        for (Pass pass : park().passes()) {
            if (pass instanceof SpecialPass specialPass && pass.getTicket().getId().equals(individualTicketId)) {
                return Optional.of(specialPass);
            }
        }
        return Optional.empty();
    }

    private IndividualTicket purchasableTicket(String ticketId) throws ParkException {
        Ticket ticket = park().findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("No ticket with identifier " + ticketId);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ValidationException("Passes are bought for individual tickets; " + ticketId
                    + " is a group ticket");
        }
        if (!individual.isActiveOn(park().getCurrentDate())) {
            throw new BusinessRuleException("Ticket " + ticketId + " is for a past date");
        }
        return individual;
    }

    private void requireFreeId(String id) throws ConflictException {
        if (park().findPass(id) != null) {
            throw new ConflictException("A pass with identifier " + id + " already exists");
        }
    }

    private static void requireNotBlank(String id) throws ValidationException {
        if (!UserValidator.isNotBlank(id)) {
            throw new ValidationException("The pass identifier must not be empty");
        }
    }

    private static String kindName(Class<? extends Pass> kind) {
        return kind == SpeedPass.class ? "SpeedPass" : "Special Pass";
    }
}
