package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.DuplicateException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.PassLookup;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * SpeedPass and Special Pass purchases, refunds and lookups (Singleton).
 */
public final class PassController implements PassLookup {

    private static final PassController INSTANCE = new PassController();

    private ParkController park;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController park) {
        this.park = park;
    }

    public List<SpeedPass> createSpeedPasses(String purchaseId, String ticketId, String quantityText) {
        int quantity = parseQuantity(quantityText);
        requireIdentifier(purchaseId);
        IndividualTicket ticket = findPurchasableTicket(ticketId);
        List<SpeedPass> created = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = SpeedPass.passId(purchaseId, n);
            requireUnusedId(id);
            created.add(SpeedPass.purchase(id, ticket));
        }
        park.passes().addAll(created);
        return created;
    }

    public SpecialPass createSpecialPass(String id, String ticketId) {
        requireIdentifier(id);
        requireUnusedId(id);
        IndividualTicket ticket = findPurchasableTicket(ticketId);
        if (findSpecialPass(ticket).isPresent()) {
            throw new OperationNotAllowedException("Ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = SpecialPass.purchase(id, ticket);
        park.passes().addAll(List.of(pass));
        return pass;
    }

    public void deleteSpeedPass(String id) {
        refund(id, SpeedPass.class);
    }

    public void deleteSpecialPass(String id) {
        refund(id, SpecialPass.class);
    }

    public static BigDecimal total(Collection<? extends Pass> passes) {
        return passes.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        return park.passes().findByTicket(ticket);
    }

    /**
     * Removes every pass of the given tickets (used when their tickets are refunded).
     */
    public void deleteAllFor(Collection<IndividualTicket> tickets) {
        List<Pass> passes = tickets.stream().flatMap(t -> passesOf(t).stream()).toList();
        park.passes().removeAll(passes);
    }

    /**
     * Persists consumption state changes.
     */
    public void save() {
        park.passes().save();
    }

    @Override
    public Optional<SpeedPass> findAvailableSpeedPass(IndividualTicket ticket) {
        return passesOf(ticket).stream()
                .filter(SpeedPass.class::isInstance)
                .map(SpeedPass.class::cast)
                .filter(p -> !p.isConsumed())
                .findFirst();
    }

    @Override
    public Optional<SpecialPass> findSpecialPass(IndividualTicket ticket) {
        return passesOf(ticket).stream()
                .filter(SpecialPass.class::isInstance)
                .map(SpecialPass.class::cast)
                .findFirst();
    }

    private void refund(String id, Class<? extends Pass> kind) {
        Pass pass = park.passes().findById(id)
                .filter(kind::isInstance)
                .orElseThrow(() -> new NotFoundException(kind.getSimpleName() + " not found: " + id));
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Pass " + id + " is consumed and cannot be refunded");
        }
        if (!pass.getTicket().isDeletableOn(park.today())) {
            throw new OperationNotAllowedException("Only passes of tickets dated after " + park.today()
                    + " can be refunded");
        }
        park.passes().removeAll(List.of(pass));
    }

    private IndividualTicket findPurchasableTicket(String ticketId) {
        IndividualTicket ticket = park.tickets().findById(ticketId)
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .orElseThrow(() -> new NotFoundException("Individual ticket not found: " + ticketId));
        if (!ticket.isActiveOn(park.today())) {
            throw new OperationNotAllowedException("Ticket " + ticketId + " is dated before " + park.today());
        }
        return ticket;
    }

    private static int parseQuantity(String text) {
        return switch (text.trim()) {
            case "1" -> 1;
            case "10" -> 10;
            default -> throw new ValidationException("Quantity must be 1 or 10: " + text);
        };
    }

    private static void requireIdentifier(String id) {
        if (id.isBlank()) {
            throw new ValidationException("Pass identifier cannot be empty");
        }
    }

    private void requireUnusedId(String id) {
        if (park.passes().existsId(id)) {
            throw new DuplicateException("Pass identifier already used: " + id);
        }
    }
}
