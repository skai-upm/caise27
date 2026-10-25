package es.upm.parque.business.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;

/**
 * Sells and refunds personal passes attached to individual tickets (Singleton).
 */
public final class PassController {

    private static PassController instance;

    private ParkController park;
    private TicketController tickets;

    private PassController() {
    }

    public static synchronized PassController getInstance() {
        if (instance == null) {
            instance = new PassController();
        }
        return instance;
    }

    public void configure(ParkController park, TicketController tickets) {
        this.park = park;
        this.tickets = tickets;
    }

    /**
     * Buys 1 or 10 SpeedPasses for an individual ticket dated on or after the system date.
     */
    public String createSpeedPasses(String ticketId, String quantityText) {
        if (!"1".equals(quantityText) && !"10".equals(quantityText)) {
            return "ERROR SpeedPasses are sold in quantities of 1 or 10";
        }
        IndividualTicket ticket = usableTicket(ticketId);
        if (ticket == null) {
            return purchaseError(ticketId);
        }
        int quantity = Integer.parseInt(quantityText);
        BigDecimal unitPrice = SpeedPass.priceFor(ticket);
        List<SpeedPass> created = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SpeedPass pass = new SpeedPass(ticket.nextSpeedPassId(), unitPrice, false);
            ticket.addSpeedPass(pass);
            created.add(pass);
            ids.add(pass.getId());
        }
        if (!tickets.save()) {
            created.forEach(ticket::removeSpeedPass);
            return "ERROR Could not save passes";
        }
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return "OK SPEEDPASS CREATED -> " + total.toPlainString() + " (" + String.join(", ", ids) + ")";
    }

    /**
     * Buys the only Special Pass an individual ticket can ever have.
     */
    public String createSpecialPass(String ticketId) {
        IndividualTicket ticket = usableTicket(ticketId);
        if (ticket == null) {
            return purchaseError(ticketId);
        }
        if (ticket.isSpecialPassPurchased()) {
            return "ERROR Ticket " + ticketId + " already had a Special Pass";
        }
        SpecialPass pass = new SpecialPass(ticket.specialPassId(), SpecialPass.priceFor(ticket), false);
        ticket.attachSpecialPass(pass);
        if (!tickets.save()) {
            return "ERROR Could not save passes";
        }
        return "OK SPECIALPASS CREATED -> " + pass.getPrice().toPlainString() + " (" + pass.getId() + ")";
    }

    public String deleteSpeedPass(String passId) {
        IndividualTicket ticket = ownerOf(passId);
        if (ticket == null || !(findPass(ticket, passId) instanceof SpeedPass pass)) {
            return "ERROR Unknown SpeedPass: " + passId;
        }
        String error = refundError(ticket, pass);
        if (error != null) {
            return error;
        }
        ticket.removeSpeedPass(pass);
        if (!tickets.save()) {
            ticket.addSpeedPass(pass);
            return "ERROR Could not save passes";
        }
        return "OK SPEEDPASS DELETED " + passId + " -> refund " + pass.getPrice().toPlainString();
    }

    public String deleteSpecialPass(String passId) {
        IndividualTicket ticket = ownerOf(passId);
        if (ticket == null || !(findPass(ticket, passId) instanceof SpecialPass pass)) {
            return "ERROR Unknown Special Pass: " + passId;
        }
        String error = refundError(ticket, pass);
        if (error != null) {
            return error;
        }
        ticket.removeSpecialPass();
        if (!tickets.save()) {
            ticket.attachSpecialPass(pass);
            return "ERROR Could not save passes";
        }
        return "OK SPECIALPASS DELETED " + passId + " -> refund " + pass.getPrice().toPlainString();
    }

    /**
     * The individual ticket owning the pass with that identifier, or {@code null}.
     */
    public IndividualTicket ownerOf(String passId) {
        for (IndividualTicket ticket : tickets.getAllIndividualTickets()) {
            if (findPass(ticket, passId) != null) {
                return ticket;
            }
        }
        return null;
    }

    private static Pass findPass(IndividualTicket ticket, String passId) {
        for (Pass pass : ticket.getPasses()) {
            if (pass.getId().equals(passId)) {
                return pass;
            }
        }
        return null;
    }

    private IndividualTicket usableTicket(String ticketId) {
        Ticket found = tickets.find(ticketId);
        if (found instanceof IndividualTicket ticket && ticket.isCurrentOrFuture(park.getCurrentDate())) {
            return ticket;
        }
        return null;
    }

    private String purchaseError(String ticketId) {
        Ticket found = tickets.find(ticketId);
        if (found == null) {
            return "ERROR Unknown ticket: " + ticketId;
        }
        if (!(found instanceof IndividualTicket)) {
            return "ERROR Passes can only be bought for individual tickets; " + ticketId + " is a group ticket";
        }
        return "ERROR Ticket " + ticketId + " is dated before " + park.getCurrentDate();
    }

    private String refundError(IndividualTicket ticket, Pass pass) {
        if (pass.isConsumed()) {
            return "ERROR " + pass.getKind() + " " + pass.getId() + " has been consumed and cannot be refunded";
        }
        if (!ticket.isFuture(park.getCurrentDate())) {
            return "ERROR Passes can only be refunded for tickets dated after " + park.getCurrentDate();
        }
        return null;
    }
}
