package es.upm.parque.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.PersonalPass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.PassRepository;

/**
 * Business rules for SpeedPasses and Special Passes attached to individual tickets.
 */
public class PassManager {

    public static final String PASS_NOT_FOUND = "ERROR PASS NOT FOUND";
    public static final String PASS_CONSUMED = "ERROR PASS CONSUMED";
    public static final String INVALID_QUANTITY = "ERROR INVALID QUANTITY";
    public static final String TICKET_EXPIRED = "ERROR TICKET EXPIRED";
    public static final String SPECIAL_ALREADY_ISSUED = "ERROR SPECIAL PASS ALREADY ISSUED";
    static final String SPEED_PREFIX = "-S";
    static final String SPECIAL_SUFFIX = "-SPECIAL";

    private static PassManager instance;

    private final PassRepository repository;
    private final TicketManager tickets;
    private final SystemDate systemDate;

    /**
     * Creates the unique PassManager with its injected dependencies, replacing any previous instance.
     * Called once at application startup, after the TicketManager.
     */
    public static PassManager initialize(PassRepository repository, TicketManager tickets, SystemDate systemDate)
            throws IOException {
        instance = new PassManager(repository, tickets, systemDate);
        return instance;
    }

    /**
     * Returns the unique PassManager; PassManager.initialize must have been called first.
     */
    public static PassManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PassManager not initialized");
        }
        return instance;
    }

    private PassManager(PassRepository repository, TicketManager tickets, SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.tickets = tickets;
        this.systemDate = systemDate;
        repository.load(tickets::findAnyIndividual);
    }

    public String createSpeedPasses(String ticketId, String quantityText) {
        if (!"1".equals(quantityText) && !"10".equals(quantityText)) {
            return INVALID_QUANTITY;
        }
        IndividualTicket ticket = tickets.findAnyIndividual(ticketId);
        if (ticket == null) {
            return TicketManager.TICKET_NOT_FOUND;
        }
        if (!ticket.isCurrentOrFuture(systemDate.get())) {
            return TICKET_EXPIRED;
        }
        int quantity = Integer.parseInt(quantityText);
        int next = nextSpeedNumber(ticket);
        BigDecimal price = SpeedPass.priceFor(ticket);
        for (int i = 0; i < quantity; i++) {
            ticket.addSpeedPass(new SpeedPass(ticket.getId() + SPEED_PREFIX + (next + i), price, false));
        }
        String first = ticket.getId() + SPEED_PREFIX + next;
        String ids = quantity == 1 ? first : first + ".." + ticket.getId() + SPEED_PREFIX + (next + quantity - 1);
        return persist("OK SPEEDPASS CREATED -> "
                + price.multiply(BigDecimal.valueOf(quantity)).toPlainString() + " (" + ids + ")");
    }

    public String createSpecialPass(String ticketId) {
        IndividualTicket ticket = tickets.findAnyIndividual(ticketId);
        if (ticket == null) {
            return TicketManager.TICKET_NOT_FOUND;
        }
        if (!ticket.isCurrentOrFuture(systemDate.get())) {
            return TICKET_EXPIRED;
        }
        if (ticket.getSpecialPass() != null) {
            return SPECIAL_ALREADY_ISSUED;
        }
        SpecialPass pass = new SpecialPass(ticket.getId() + SPECIAL_SUFFIX, SpecialPass.priceFor(ticket), false);
        ticket.setSpecialPass(pass);
        return persist("OK SPECIALPASS CREATED -> " + pass.getPrice().toPlainString() + " (" + pass.getId() + ")");
    }

    public String deleteSpeedPass(String passId) {
        for (IndividualTicket ticket : tickets.allIndividualTickets()) {
            for (SpeedPass pass : ticket.getSpeedPasses()) {
                if (pass.getId().equals(passId)) {
                    String error = checkRefundable(ticket, pass);
                    if (error != null) {
                        return error;
                    }
                    ticket.removeSpeedPass(pass);
                    return persist("OK SPEEDPASS DELETED -> " + pass.getPrice().toPlainString());
                }
            }
        }
        return PASS_NOT_FOUND;
    }

    public String deleteSpecialPass(String ticketId) {
        IndividualTicket ticket = tickets.findAnyIndividual(ticketId);
        SpecialPass pass = ticket == null ? null : ticket.getSpecialPass();
        if (pass == null) {
            return PASS_NOT_FOUND;
        }
        String error = checkRefundable(ticket, pass);
        if (error != null) {
            return error;
        }
        ticket.setSpecialPass(null);
        return persist("OK SPECIALPASS DELETED -> " + pass.getPrice().toPlainString());
    }

    /**
     * Marks a pass as used and stores the new status.
     */
    public String consume(PersonalPass pass) {
        pass.consume();
        return persist("OK PASS CONSUMED");
    }

    private String checkRefundable(IndividualTicket ticket, PersonalPass pass) {
        if (pass.isConsumed()) {
            return PASS_CONSUMED;
        }
        return ticket.isRefundable(systemDate.get()) ? null : TicketManager.NOT_REFUNDABLE;
    }

    private static int nextSpeedNumber(IndividualTicket ticket) {
        int max = 0;
        String prefix = ticket.getId() + SPEED_PREFIX;
        for (SpeedPass pass : ticket.getSpeedPasses()) {
            String suffix = pass.getId().startsWith(prefix) ? pass.getId().substring(prefix.length()) : "";
            if (!suffix.isEmpty() && suffix.chars().allMatch(Character::isDigit)) {
                max = Math.max(max, Integer.parseInt(suffix));
            }
        }
        return max + 1;
    }

    private String persist(String success) {
        try {
            List<IndividualTicket> all = tickets.allIndividualTickets();
            repository.save(all);
            return success;
        } catch (IOException e) {
            return UserManager.STORAGE_FAILURE;
        }
    }
}
