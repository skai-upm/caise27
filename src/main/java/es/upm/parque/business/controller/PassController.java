package es.upm.parque.business.controller;

import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.repository.PassRepository;

/**
 * Purchase and refund of personal passes attached to individual tickets. Singleton.
 */
public final class PassController {

    private static final PassController INSTANCE = new PassController();
    private static final String SPEED_SUFFIX = "-SP";
    private static final String SPECIAL_SUFFIX = "-SPECIAL";
    private static final int SINGLE = 1;
    private static final int PACK = 10;

    private PassRepository repository;
    private ParkController park;
    private TicketController tickets;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(PassRepository repository, ParkController park, TicketController tickets) {
        this.repository = repository;
        this.park = park;
        this.tickets = tickets;
    }

    /**
     * Attaches the stored passes to the loaded tickets. Tickets must be loaded first.
     */
    public void load() {
        repository.load(tickets::findIndividual);
    }

    public void save() {
        repository.save(tickets.getIndividualTickets());
    }

    /**
     * Buys 1 or 10 independent SpeedPasses, each costing 20% of the ticket price.
     */
    public List<SpeedPass> buySpeedPasses(String ticketId, String quantityText) {
        IndividualTicket ticket = purchasableTicket(ticketId);
        int quantity = parseQuantity(quantityText);
        int next = ticket.getSpeedPasses().stream()
                .mapToInt(p -> suffixNumber(p.getId(), ticket.getId()))
                .max().orElse(0) + 1;
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SpeedPass pass = new SpeedPass(ticket.getId() + SPEED_SUFFIX + (next + i),
                    SpeedPass.priceFor(ticket), ticket, false);
            ticket.addSpeedPass(pass);
            bought.add(pass);
        }
        save();
        return bought;
    }

    /**
     * Buys the ticket's Special Pass, costing the ticket price. A ticket holds at most one.
     */
    public SpecialPass buySpecialPass(String ticketId) {
        IndividualTicket ticket = purchasableTicket(ticketId);
        if (ticket.getSpecialPass() != null) {
            throw new IllegalArgumentException("Ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(ticket.getId() + SPECIAL_SUFFIX, SpecialPass.priceFor(ticket),
                ticket, false);
        ticket.setSpecialPass(pass);
        save();
        return pass;
    }

    public SpeedPass refundSpeedPass(String passId) {
        SpeedPass pass = tickets.getIndividualTickets().stream()
                .flatMap(t -> t.getSpeedPasses().stream())
                .filter(p -> p.getId().equals(passId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SpeedPass not found: " + passId));
        requireRefundable(pass);
        pass.getTicket().removeSpeedPass(pass);
        save();
        return pass;
    }

    public SpecialPass refundSpecialPass(String ticketId) {
        IndividualTicket ticket = tickets.getIndividual(ticketId);
        SpecialPass pass = ticket.getSpecialPass();
        if (pass == null) {
            throw new IllegalArgumentException("Ticket " + ticketId + " has no Special Pass");
        }
        requireRefundable(pass);
        ticket.setSpecialPass(null);
        save();
        return pass;
    }

    private IndividualTicket purchasableTicket(String ticketId) {
        IndividualTicket ticket = tickets.getIndividual(ticketId);
        if (!ticket.isCurrentOrFuture(park.getSystemDate())) {
            throw new IllegalArgumentException("Ticket " + ticketId + " is in the past");
        }
        return ticket;
    }

    private void requireRefundable(Pass pass) {
        if (pass.isConsumed()) {
            throw new IllegalArgumentException(pass.getKind() + " " + pass.getId() + " was already used");
        }
        if (!pass.getTicket().isRefundable(park.getSystemDate())) {
            throw new IllegalArgumentException(pass.getKind() + " " + pass.getId()
                    + " can only be refunded before the ticket date " + pass.getTicket().getDate());
        }
    }

    private static int parseQuantity(String text) {
        if (String.valueOf(SINGLE).equals(text)) {
            return SINGLE;
        }
        if (String.valueOf(PACK).equals(text)) {
            return PACK;
        }
        throw new IllegalArgumentException("SpeedPasses are sold individually (1) or in packs of 10, not " + text);
    }

    private static int suffixNumber(String passId, String ticketId) {
        try {
            return Integer.parseInt(passId.substring(ticketId.length() + SPEED_SUFFIX.length()));
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
