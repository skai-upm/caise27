package es.upm.park.business.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import es.upm.park.business.model.IndividualAccess;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.persistence.repository.PassRepository;

/**
 * Controller of SpeedPasses and Special Passes.
 */
public class PassService {

    private static final PassService INSTANCE = new PassService();
    private static final int SINGLE = 1;
    private static final int PACKAGE = 10;

    private PassRepository passes;
    private TicketService tickets;
    private Supplier<LocalDate> today;

    private PassService() {
    }

    /** The single PassService instance of the execution. */
    public static PassService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies chosen during application initialization.
     *
     * @return this controller
     */
    public PassService configure(PassRepository passes, TicketService tickets, Supplier<LocalDate> today) {
        this.passes = passes;
        this.tickets = tickets;
        this.today = today;
        return this;
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code purchase:1..n}; nothing is
     * created if any identifier is taken.
     */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) {
        int count = parseQuantity(quantity);
        IndividualTicket ticket = usableTicket(ticketId);
        List<SpeedPass> created = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String id = purchaseId + ":" + i;
            checkFreeId(id);
            created.add(new SpeedPass(id, ticket));
        }
        passes.addAll(created);
        return created;
    }

    /** Buys the single Special Pass of an individual ticket. */
    public SpecialPass buySpecialPass(String id, String ticketId) {
        if (id == null || id.isBlank()) {
            throw new PassException("pass identifier is required");
        }
        IndividualTicket ticket = usableTicket(ticketId);
        checkFreeId(id);
        if (passes.findByTicket(ticket).stream().anyMatch(SpecialPass.class::isInstance)) {
            throw new PassException("ticket " + ticketId + " already has a special pass");
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.addAll(List.of(pass));
        return pass;
    }

    public SpeedPass refundSpeedPass(String id) {
        return refund(id, SpeedPass.class, "speed pass");
    }

    public SpecialPass refundSpecialPass(String id) {
        return refund(id, SpecialPass.class, "special pass");
    }

    /** Removes every pass of the given tickets (their tickets were refunded). */
    public void removeAllFor(List<IndividualTicket> refundedTickets) {
        for (IndividualTicket ticket : refundedTickets) {
            passes.removeAll(passes.findByTicket(ticket));
        }
    }

    public List<Pass> passesOf(IndividualAccess ticket) {
        return passes.findByTicket(ticket);
    }

    private <P extends Pass> P refund(String id, Class<P> type, String name) {
        P pass = passes.findById(id).filter(type::isInstance).map(type::cast)
                .orElseThrow(() -> new PassException("no " + name + " " + id));
        if (pass.isConsumed()) {
            throw new PassException(name + " " + id + " has been consumed");
        }
        if (!pass.isRefundable(today.get())) {
            throw new PassException(name + " " + id + " cannot be refunded: its ticket is not dated after "
                    + today.get());
        }
        passes.removeAll(List.of(pass));
        return pass;
    }

    private IndividualTicket usableTicket(String ticketId) {
        IndividualTicket ticket = tickets.findIndividual(ticketId);
        if (ticket.getDate().isBefore(today.get())) {
            throw new PassException("ticket " + ticketId + " is dated before the current date " + today.get());
        }
        return ticket;
    }

    private void checkFreeId(String id) {
        if (passes.existsId(id)) {
            throw new PassException("pass identifier " + id + " already exists");
        }
    }

    private static int parseQuantity(String quantity) {
        if (String.valueOf(SINGLE).equals(quantity)) {
            return SINGLE;
        }
        if (String.valueOf(PACKAGE).equals(quantity)) {
            return PACKAGE;
        }
        throw new PassException("quantity must be 1 or 10");
    }

    /** Sum of the prices of the given passes. */
    public static BigDecimal total(List<? extends Pass> list) {
        return list.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
