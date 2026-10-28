package es.upm.amusementpark.business.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassLookup;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.repository.PassRepository;

/**
 * Purchase and refund of SpeedPasses and Special Passes.
 */
public class PassService implements PassLookup {

    private static final PassService INSTANCE = new PassService();

    private PassRepository repository;
    private TicketService tickets;
    private Supplier<LocalDate> today;

    private PassService() {
    }

    public static PassService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies of this controller; called during application initialization.
     */
    public PassService configure(PassRepository repository, TicketService tickets, Supplier<LocalDate> today) {
        this.repository = repository;
        this.tickets = tickets;
        this.today = today;
        return this;
    }

    public List<Pass> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Pass> passesOf(IndividualTicket ticket) {
        return repository.findByTicket(ticket);
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}; all identifiers are checked
     * before anything is created.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, int quantity) throws ParkException {
        if (quantity != 1 && quantity != 10) {
            throw new ValidationException("SpeedPass quantity must be 1 or 10");
        }
        IndividualTicket ticket = usableTicket(ticketId);
        List<SpeedPass> passes = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String id = SpeedPass.derivedId(purchaseId, i);
            requireNewIdentifier(id);
            passes.add(new SpeedPass(id, ticket));
        }
        passes.forEach(repository::save);
        return passes;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        requireNewIdentifier(id);
        IndividualTicket ticket = usableTicket(ticketId);
        if (repository.findByTicket(ticket).stream().anyMatch(SpecialPass.class::isInstance)) {
            throw new ConflictException("Ticket already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        repository.save(pass);
        return pass;
    }

    public void refundSpeedPass(String id) throws ParkException {
        refund(id, SpeedPass.class, "SpeedPass");
    }

    public void refundSpecialPass(String id) throws ParkException {
        refund(id, SpecialPass.class, "Special Pass");
    }

    /**
     * Removes every pass of the given tickets (joint refund when their ticket is deleted).
     */
    public void deletePassesOf(Collection<IndividualTicket> deletedTickets) {
        for (IndividualTicket ticket : deletedTickets) {
            repository.findByTicket(ticket).forEach(p -> repository.delete(p.getId()));
        }
    }

    public static BigDecimal total(List<? extends Pass> passes) {
        return passes.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private void refund(String id, Class<? extends Pass> type, String name) throws ParkException {
        Pass pass = repository.findById(id).filter(type::isInstance)
                .orElseThrow(() -> new NotFoundException(name + " not found"));
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("A consumed pass cannot be refunded");
        }
        if (!pass.isRefundableOn(today.get())) {
            throw new OperationNotAllowedException("Only passes of tickets dated after the current date can be refunded");
        }
        repository.delete(id);
    }

    private IndividualTicket usableTicket(String ticketId) throws ParkException {
        IndividualTicket ticket = tickets.findIndividualById(ticketId)
                .orElseThrow(() -> new NotFoundException("Individual ticket not found"));
        if (ticket.getDate().isBefore(today.get())) {
            throw new OperationNotAllowedException("Passes cannot be bought for past tickets");
        }
        return ticket;
    }

    private void requireNewIdentifier(String id) throws ConflictException {
        if (repository.findById(id).isPresent()) {
            throw new ConflictException("A pass with this identifier already exists: " + id);
        }
    }
}
