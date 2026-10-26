package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for personal passes: buying SpeedPasses (1 or 10 at a time) and the Special Pass of
 * an individual ticket, and refunding unused passes. Passes are stored with their tickets.
 */
public final class PassController {

    private static final String SPEED_PREFIX = "SP";
    private static final String SPECIAL_PREFIX = "XP";

    private TicketController ticketController;
    private MainController mainController;

    private PassController() {
    }

    /**
     * Returns the single instance of this controller.
     */
    public static PassController getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Injects the collaborating controllers chosen at startup.
     */
    public void configure(TicketController ticketController, MainController mainController) {
        this.ticketController = ticketController;
        this.mainController = mainController;
    }

    /**
     * Buys 1 or 10 SpeedPasses for an individual ticket and returns them.
     */
    public List<SpeedPass> buySpeedPasses(String ticketId, String quantityText) {
        requireConfigured();
        if (!"1".equals(quantityText) && !"10".equals(quantityText)) {
            throw new ParkException("SpeedPasses are sold one at a time or in packs of 10: " + quantityText);
        }
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        int next = nextNumber(SPEED_PREFIX);
        BigDecimal price = SpeedPass.priceFor(ticket.getPrice());
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(quantityText); i++) {
            SpeedPass pass = new SpeedPass(SPEED_PREFIX + (next + i), price, false);
            ticket.addSpeedPass(pass);
            bought.add(pass);
        }
        ticketController.save();
        return bought;
    }

    /**
     * Buys the Special Pass of an individual ticket, which may only have one.
     */
    public SpecialPass buySpecialPass(String ticketId) {
        requireConfigured();
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        if (ticket.getSpecialPass() != null) {
            throw new ParkException("Ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(SPECIAL_PREFIX + nextNumber(SPECIAL_PREFIX), ticket.getPrice(), false);
        ticket.setSpecialPass(pass);
        ticketController.save();
        return pass;
    }

    /**
     * Refunds an unused SpeedPass and returns it.
     */
    public SpeedPass refundSpeedPass(String passId) {
        requireConfigured();
        IndividualTicket ticket = requireOwner(passId);
        if (!(passOf(ticket, passId) instanceof SpeedPass pass)) {
            throw new ParkException("No SpeedPass with identifier " + passId);
        }
        requireRefundable(ticket, pass);
        ticket.removeSpeedPass(pass);
        ticketController.save();
        return pass;
    }

    /**
     * Refunds an unused Special Pass and returns it.
     */
    public SpecialPass refundSpecialPass(String passId) {
        requireConfigured();
        IndividualTicket ticket = requireOwner(passId);
        if (!(passOf(ticket, passId) instanceof SpecialPass pass)) {
            throw new ParkException("No Special Pass with identifier " + passId);
        }
        requireRefundable(ticket, pass);
        ticket.setSpecialPass(null);
        ticketController.save();
        return pass;
    }

    /**
     * The pass with that identifier, or {@code null} if there is none.
     */
    public Pass findPass(String passId) {
        requireConfigured();
        IndividualTicket ticket = ticketOf(passId);
        return ticket == null ? null : passOf(ticket, passId);
    }

    /**
     * The individual ticket that owns the pass, or {@code null} if no ticket has it.
     */
    public IndividualTicket ticketOf(String passId) {
        requireConfigured();
        return ticketController.getIndividualTickets().stream()
                .filter(t -> t.getPasses().stream().anyMatch(p -> p.getId().equals(passId)))
                .findFirst().orElse(null);
    }

    private IndividualTicket requireOwner(String passId) {
        IndividualTicket ticket = ticketOf(passId);
        if (ticket == null) {
            throw new ParkException("No pass with identifier " + passId);
        }
        return ticket;
    }

    private static Pass passOf(IndividualTicket ticket, String passId) {
        return ticket.getPasses().stream().filter(p -> p.getId().equals(passId)).findFirst().orElse(null);
    }

    private IndividualTicket requirePurchasableTicket(String ticketId) {
        IndividualTicket ticket = ticketController.findIndividualTicket(ticketId);
        if (ticket == null) {
            String reason = ticketController.findById(ticketId) != null
                    ? "Passes belong to individual tickets; " + ticketId + " is a group ticket"
                    : "No ticket with identifier " + ticketId;
            throw new ParkException(reason);
        }
        if (ticket.getDate().isBefore(mainController.getCurrentDate())) {
            throw new ParkException("Cannot buy passes for past ticket " + ticketId);
        }
        return ticket;
    }

    private void requireRefundable(IndividualTicket ticket, Pass pass) {
        if (pass.isConsumed()) {
            throw new ParkException("Pass " + pass.getId() + " has been consumed and cannot be refunded");
        }
        if (!ticket.getDate().isAfter(mainController.getCurrentDate())) {
            throw new ParkException("Passes can only be refunded before the date of their ticket");
        }
    }

    /**
     * One more than the highest number used by existing passes with the given prefix.
     */
    private int nextNumber(String prefix) {
        int max = 0;
        for (IndividualTicket t : ticketController.getIndividualTickets()) {
            for (Pass p : t.getPasses()) {
                String digits = p.getId().startsWith(prefix) ? p.getId().substring(prefix.length()) : "";
                if (!digits.isEmpty() && digits.chars().allMatch(Character::isDigit)) {
                    max = Math.max(max, Integer.parseInt(digits));
                }
            }
        }
        return max + 1;
    }

    private void requireConfigured() {
        if (ticketController == null) {
            throw new IllegalStateException("PassController is not configured");
        }
    }

    private static final class Holder {
        static final PassController INSTANCE = new PassController();
    }
}
