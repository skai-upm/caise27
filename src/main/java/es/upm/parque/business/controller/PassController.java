package es.upm.parque.business.controller;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.repository.PassRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for SpeedPasses and Special Passes: purchase, refund and lookup.
 */
public class PassController {
    private static final PassController INSTANCE = new PassController();
    private static final String SPEED_PASS_INFIX = "-SP";
    private static final String SPECIAL_PASS_SUFFIX = "-SPECIAL";

    private PassRepository repository;
    private TicketController ticketController;
    private SystemDate systemDate;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and collaborators and attaches the stored passes to the tickets of the ticket controller
     * (which must already be configured).
     */
    public PassController configure(PassRepository repository, TicketController ticketController,
            SystemDate systemDate) {
        this.repository = repository;
        this.ticketController = ticketController;
        this.systemDate = systemDate;
        repository.load(id -> ticketController.findById(id) instanceof IndividualTicket it ? it : null);
        return this;
    }

    public List<SpeedPass> buySpeedPasses(String ticketId, int quantity) {
        if (quantity != 1 && quantity != 10) {
            throw new IllegalArgumentException("SpeedPasses are sold individually or in packs of 10, not " + quantity);
        }
        IndividualTicket ticket = requireUsableTicket(ticketId);
        int next = nextSpeedPassNumber(ticket);
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SpeedPass pass = new SpeedPass(ticket.getId() + SPEED_PASS_INFIX + (next + i), SpeedPass.priceFor(ticket),
                    false);
            ticket.addSpeedPass(pass);
            bought.add(pass);
        }
        save();
        return bought;
    }

    public SpecialPass buySpecialPass(String ticketId) {
        IndividualTicket ticket = requireUsableTicket(ticketId);
        SpecialPass pass = new SpecialPass(ticket.getId() + SPECIAL_PASS_SUFFIX, SpecialPass.priceFor(ticket), false);
        ticket.setSpecialPass(pass);
        save();
        return pass;
    }

    public SpeedPass refundSpeedPass(String passId) {
        return (SpeedPass) refund(passId, SpeedPass.class);
    }

    public SpecialPass refundSpecialPass(String passId) {
        return (SpecialPass) refund(passId, SpecialPass.class);
    }

    /** Persists the current state of every pass (e.g. after passes are consumed). */
    public void save() {
        repository.save(individualTickets());
    }

    private Pass refund(String passId, Class<? extends Pass> kind) {
        for (IndividualTicket ticket : individualTickets()) {
            for (Pass pass : ticket.getPasses()) {
                if (pass.getId().equals(passId) && kind.isInstance(pass)) {
                    if (pass.isConsumed()) {
                        throw new IllegalArgumentException(
                                "pass " + passId + " has been consumed and cannot be refunded");
                    }
                    if (!ticket.isFuture(systemDate.get())) {
                        throw new IllegalArgumentException("pass " + passId + " can only be refunded before its ticket"
                                + " date " + ticket.getDate() + " (system date " + systemDate.get() + ")");
                    }
                    ticket.removePass(pass);
                    save();
                    return pass;
                }
            }
        }
        String kindName = kind == SpeedPass.class ? "SpeedPass" : "Special Pass";
        throw new IllegalArgumentException("no " + kindName + " with id " + passId);
    }

    private IndividualTicket requireUsableTicket(String ticketId) {
        Ticket ticket = ticketController.findById(ticketId);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new IllegalArgumentException(
                    "passes require an existing individual ticket; " + ticketId + " is not one");
        }
        if (!individual.isCurrentOrFuture(systemDate.get())) {
            throw new IllegalArgumentException(
                    "ticket " + ticketId + " is for a past date (" + individual.getDate() + ")");
        }
        return individual;
    }

    private static int nextSpeedPassNumber(IndividualTicket ticket) {
        int max = 0;
        String prefix = ticket.getId() + SPEED_PASS_INFIX;
        for (SpeedPass pass : ticket.getSpeedPasses()) {
            try {
                max = Math.max(max, Integer.parseInt(pass.getId().substring(prefix.length())));
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                // identifiers not generated by this controller do not affect numbering
            }
        }
        return max + 1;
    }

    private List<IndividualTicket> individualTickets() {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : ticketController.getTickets()) {
            if (t instanceof IndividualTicket it) {
                result.add(it);
            }
        }
        return result;
    }
}
