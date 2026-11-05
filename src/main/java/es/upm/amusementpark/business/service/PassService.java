package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.RuleViolationException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase and refund of SpeedPasses and Special Passes.
 */
public class PassService {

    private TicketRepository tickets;
    private PassRepository passes;

    private PassService() {
    }

    private static final class Holder {
        private static final PassService INSTANCE = new PassService();
    }

    /** The single instance of this controller. */
    public static PassService getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the repositories this controller works with (done during application initialization). */
    public PassService inject(TicketRepository tickets, PassRepository passes) {
        this.tickets = tickets;
        this.passes = passes;
        return this;
    }

    public List<SpeedPass> createSpeedPasses(String purchaseId, String ticketId, String quantity) {
        if (!"1".equals(quantity) && !"10".equals(quantity)) {
            throw new InvalidDataException("invalid quantity '" + quantity + "', expected 1 or 10");
        }
        if (purchaseId.isEmpty()) {
            throw new InvalidDataException("purchase identifier must not be empty");
        }
        IndividualTicket ticket = activeTicket(ticketId);
        List<SpeedPass> created = new ArrayList<>();
        for (int i = 1; i <= Integer.parseInt(quantity); i++) {
            created.add(new SpeedPass(purchaseId + ":" + i, ticket));
        }
        passes.addAll(created);
        return created;
    }

    public SpecialPass createSpecialPass(String identifier, String ticketId) {
        if (identifier.isEmpty()) {
            throw new InvalidDataException("pass identifier must not be empty");
        }
        IndividualTicket ticket = activeTicket(ticketId);
        if (passes.passesOf(ticket).stream().anyMatch(SpecialPass.class::isInstance)) {
            throw new RuleViolationException("ticket '" + ticketId + "' already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(identifier, ticket);
        passes.addAll(List.of(pass));
        return pass;
    }

    public void refund(String identifier, Class<? extends Pass> type) {
        Pass pass = passes.findById(identifier).filter(type::isInstance)
                .orElseThrow(() -> new NotFoundException("pass '" + identifier + "' not found"));
        LocalDate today = Park.getInstance().getCurrentDate();
        if (pass.isConsumed()) {
            throw new RuleViolationException("pass '" + identifier + "' has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(today)) {
            throw new RuleViolationException("only passes of tickets dated after the current date (" + today + ") can be refunded");
        }
        passes.removeAll(List.of(pass));
    }

    public static BigDecimal total(List<? extends Pass> list) {
        return list.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private IndividualTicket activeTicket(String ticketId) {
        IndividualTicket ticket = tickets.findById(ticketId).filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .orElseThrow(() -> new NotFoundException("individual ticket '" + ticketId + "' not found"));
        LocalDate today = Park.getInstance().getCurrentDate();
        if (ticket.getDate().isBefore(today)) {
            throw new RuleViolationException("passes can only be bought for tickets dated " + today + " or later");
        }
        return ticket;
    }
}
