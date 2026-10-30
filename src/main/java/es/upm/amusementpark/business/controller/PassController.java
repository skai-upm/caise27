package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Formats;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase and refund of personal passes attached to individual tickets.
 */
public class PassController {

    private static PassController instance;

    private TicketController ticketController;

    private PassController() {
    }

    /** Returns the unique instance of this controller. */
    public static synchronized PassController getInstance() {
        if (instance == null) {
            instance = new PassController();
        }
        return instance;
    }

    /** Injects the ticket controller that owns the tickets the passes belong to. */
    public void configure(TicketController ticketController) {
        this.ticketController = ticketController;
    }

    public String buySpeedPasses(String ticketId, String quantityText, LocalDate today) {
        String quantity = quantityText.trim();
        if (!quantity.equals("1") && !quantity.equals("10")) {
            return "ERROR: SpeedPasses are sold in quantities of 1 or 10";
        }
        IndividualTicket ticket = eligibleTicket(ticketId, today);
        if (ticket == null) {
            return ineligible(ticketId, today);
        }
        List<String> ids = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < Integer.parseInt(quantity); i++) {
            SpeedPass pass = ticket.addSpeedPass();
            ids.add(pass.getId());
            total = total.add(pass.getPrice());
        }
        return "OK SPEEDPASS CREATED -> " + Formats.formatPrice(total) + " " + ids;
    }

    public String buySpecialPass(String ticketId, LocalDate today) {
        IndividualTicket ticket = eligibleTicket(ticketId, today);
        if (ticket == null) {
            return ineligible(ticketId, today);
        }
        if (ticket.getSpecialPass() != null) {
            return "ERROR: ticket " + ticketId + " already has a Special Pass";
        }
        SpecialPass pass = ticket.createSpecialPass();
        ticket.setSpecialPass(pass);
        return "OK SPECIALPASS CREATED -> " + Formats.formatPrice(pass.getPrice()) + " [" + pass.getId() + "]";
    }

    public String refundSpeedPass(String passId, LocalDate today) {
        for (IndividualTicket ticket : ticketController.getAllIndividualTickets()) {
            for (SpeedPass pass : ticket.getSpeedPasses()) {
                if (pass.getId().equals(passId)) {
                    String error = checkRefund(pass, ticket, today);
                    if (error != null) {
                        return error;
                    }
                    ticket.removeSpeedPass(pass);
                    return refunded(pass);
                }
            }
        }
        return unknownPass(passId, "SpeedPass");
    }

    public String refundSpecialPass(String passId, LocalDate today) {
        for (IndividualTicket ticket : ticketController.getAllIndividualTickets()) {
            SpecialPass pass = ticket.getSpecialPass();
            if (pass != null && pass.getId().equals(passId)) {
                String error = checkRefund(pass, ticket, today);
                if (error != null) {
                    return error;
                }
                ticket.setSpecialPass(null);
                return refunded(pass);
            }
        }
        return unknownPass(passId, "Special Pass");
    }

    private IndividualTicket eligibleTicket(String ticketId, LocalDate today) {
        Ticket ticket = ticketController.findById(ticketId);
        if (ticket instanceof IndividualTicket individual && individual.isActiveOn(today)) {
            return individual;
        }
        return null;
    }

    private String ineligible(String ticketId, LocalDate today) {
        Ticket ticket = ticketController.findById(ticketId);
        if (ticket == null) {
            return "ERROR: ticket " + ticketId + " does not exist";
        }
        if (!(ticket instanceof IndividualTicket)) {
            return "ERROR: passes can only be bought for individual tickets";
        }
        return "ERROR: ticket " + ticketId + " is dated before " + today;
    }

    private static String checkRefund(Pass pass, IndividualTicket ticket, LocalDate today) {
        if (pass.isConsumed()) {
            return "ERROR: " + pass.getKind() + " " + pass.getId() + " has been used and cannot be refunded";
        }
        if (!ticket.isRefundableOn(today)) {
            return "ERROR: " + pass.getKind() + " " + pass.getId() + " can only be refunded before the ticket date";
        }
        return null;
    }

    private static String refunded(Pass pass) {
        return "OK: " + pass.getKind() + " " + pass.getId() + " refunded (" + Formats.formatPrice(pass.getPrice()) + ")";
    }

    private static String unknownPass(String passId, String kind) {
        return "ERROR: " + kind + " " + passId + " does not exist";
    }
}
