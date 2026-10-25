package com.amusementpark.business.controller;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import com.amusementpark.business.model.SpecialPass;
import com.amusementpark.business.model.SpeedPass;
import com.amusementpark.business.model.Ticket;
import com.amusementpark.business.model.User;
import com.amusementpark.business.validation.Validator;
import com.amusementpark.persistence.GroupTicketRepository;
import com.amusementpark.persistence.PassRepository;
import com.amusementpark.persistence.TicketRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sells and deletes individual and group tickets and the personal passes attached to
 * individual tickets.
 */
public final class TicketController {
    private static final TicketController INSTANCE = new TicketController();

    private TicketRepository ticketRepository;
    private GroupTicketRepository groupTicketRepository;
    private PassRepository passRepository;
    private ParkController parkController;
    private UserController userController;
    private GroupController groupController;
    private final List<IndividualTicket> tickets = new ArrayList<>();
    private final List<GroupTicket> groupTickets = new ArrayList<>();

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(TicketRepository ticketRepository, GroupTicketRepository groupTicketRepository,
                          PassRepository passRepository, ParkController parkController,
                          UserController userController, GroupController groupController) {
        this.ticketRepository = ticketRepository;
        this.groupTicketRepository = groupTicketRepository;
        this.passRepository = passRepository;
        this.parkController = parkController;
        this.userController = userController;
        this.groupController = groupController;
        tickets.clear();
        groupTickets.clear();
    }

    public void load() throws IOException {
        List<User> users = userController.getUsers();
        groupTickets.clear();
        groupTickets.addAll(groupTicketRepository.load(users, groupController.getGroups()));
        tickets.clear();
        tickets.addAll(ticketRepository.load(users));
        passRepository.load(allIndividualTickets());
    }

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    public String createTicket(String id, String email, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return "ERROR: unknown user " + email;
        }
        IndividualTicket ticket = new IndividualTicket(id, Validator.parseDate(dateText),
                parkController.getTicketPrice(), user);
        tickets.add(ticket);
        if (!saveTickets()) {
            tickets.remove(ticket);
            return "ERROR: could not save tickets";
        }
        return "OK TICKET CREATED -> " + ParkController.format(ticket.getPrice());
    }

    public String deleteTicket(String id) {
        IndividualTicket ticket = null;
        for (IndividualTicket candidate : tickets) {
            if (candidate.getId().equals(id)) {
                ticket = candidate;
            }
        }
        if (ticket == null) {
            return findTicket(id) != null
                    ? "ERROR: ticket " + id + " belongs to a group ticket, use GROUPTICKET DELETE"
                    : "ERROR: unknown ticket " + id;
        }
        LocalDate today = parkController.getCurrentDate();
        if (!ticket.isFutureOn(today)) {
            return "ERROR: only tickets dated after " + today + " can be deleted";
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        if (!saveTickets() || !savePasses()) {
            tickets.add(index, ticket);
            restoreFiles();
            return "ERROR: could not save tickets";
        }
        return "OK: ticket deleted with " + passCount(ticket) + " pass(es)";
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        Group group = groupController.findGroup(groupId);
        if (group == null) {
            return "ERROR: unknown group " + groupId;
        }
        if (group.getMembers().isEmpty()) {
            return "ERROR: group " + groupId + " has no members";
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, Validator.parseDate(dateText),
                parkController.getTicketPrice(), group);
        for (IndividualTicket ticket : groupTicket.getTickets()) {
            if (findTicket(ticket.getId()) != null) {
                return "ERROR: ticket id " + ticket.getId() + " already exists";
            }
        }
        groupTickets.add(groupTicket);
        if (!saveGroupTickets()) {
            groupTickets.remove(groupTicket);
            return "ERROR: could not save group tickets";
        }
        return "OK GROUP TICKET CREATED -> " + ParkController.format(groupTicket.getPrice());
    }

    public String deleteGroupTicket(String id) {
        GroupTicket groupTicket = findGroupTicket(id);
        if (groupTicket == null) {
            return "ERROR: unknown group ticket " + id;
        }
        LocalDate today = parkController.getCurrentDate();
        if (!groupTicket.isFutureOn(today)) {
            return "ERROR: only group tickets dated after " + today + " can be deleted";
        }
        int passes = 0;
        for (IndividualTicket ticket : groupTicket.getTickets()) {
            passes += passCount(ticket);
        }
        // Passes go with their member tickets, which go with the group ticket; all files are
        // rewritten from the new state, or restored from the old one if any write fails.
        int index = groupTickets.indexOf(groupTicket);
        groupTickets.remove(index);
        if (!savePasses() || !saveGroupTickets()) {
            groupTickets.add(index, groupTicket);
            restoreFiles();
            return "ERROR: could not save group tickets";
        }
        return "OK: group ticket deleted with " + groupTicket.getTickets().size() + " individual tickets and "
                + passes + " pass(es)";
    }

    // ---- Passes ----

    public String buySpeedPasses(String ticketId, String quantityText) {
        int quantity;
        if ("1".equals(quantityText)) {
            quantity = 1;
        } else if ("10".equals(quantityText)) {
            quantity = 10;
        } else {
            return "ERROR: SpeedPasses are sold in quantities of 1 or 10";
        }
        IndividualTicket ticket = findPassableTicket(ticketId);
        if (ticket == null) {
            return passableTicketError(ticketId);
        }
        List<SpeedPass> bought = ticket.buySpeedPasses(quantity);
        if (!savePasses()) {
            for (SpeedPass speedPass : bought) {
                ticket.remove(speedPass);
            }
            return "ERROR: could not save passes";
        }
        BigDecimal total = BigDecimal.ZERO;
        for (SpeedPass speedPass : bought) {
            total = total.add(speedPass.getPrice());
        }
        String ids = bought.size() == 1 ? bought.get(0).getId()
                : bought.get(0).getId() + " .. " + bought.get(bought.size() - 1).getId();
        return "OK SPEEDPASS CREATED -> " + ParkController.format(total) + " (" + ids + ")";
    }

    public String buySpecialPass(String ticketId) {
        IndividualTicket ticket = findPassableTicket(ticketId);
        if (ticket == null) {
            return passableTicketError(ticketId);
        }
        SpecialPass specialPass = ticket.buySpecialPass();
        if (specialPass == null) {
            return "ERROR: ticket " + ticketId + " already has a Special Pass";
        }
        if (!savePasses()) {
            ticket.remove(specialPass);
            return "ERROR: could not save passes";
        }
        return "OK SPECIALPASS CREATED -> " + ParkController.format(specialPass.getPrice())
                + " (" + specialPass.getId() + ")";
    }

    public String refundSpeedPass(String passId) {
        return refund(passId, SpeedPass.class, "SpeedPass");
    }

    public String refundSpecialPass(String passId) {
        return refund(passId, SpecialPass.class, "Special Pass");
    }

    private String refund(String passId, Class<? extends Pass> kind, String label) {
        for (IndividualTicket ticket : allIndividualTickets()) {
            Pass pass = ticket.findPass(passId);
            if (pass == null || !kind.isInstance(pass)) {
                continue;
            }
            if (pass.isConsumed()) {
                return "ERROR: " + label + " " + passId + " has been used";
            }
            LocalDate today = parkController.getCurrentDate();
            if (!ticket.isFutureOn(today)) {
                return "ERROR: passes can only be refunded for tickets dated after " + today;
            }
            ticket.remove(pass);
            if (!savePasses()) {
                ticket.attach(pass);
                return "ERROR: could not save passes";
            }
            return "OK: " + label + " " + passId + " refunded -> " + ParkController.format(pass.getPrice());
        }
        return "ERROR: unknown " + label + " " + passId;
    }

    /**
     * An individual ticket that can receive new passes (dated on or after the system date), or null.
     */
    private IndividualTicket findPassableTicket(String ticketId) {
        if (findTicket(ticketId) instanceof IndividualTicket ticket
                && ticket.isActiveOn(parkController.getCurrentDate())) {
            return ticket;
        }
        return null;
    }

    private String passableTicketError(String ticketId) {
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            return "ERROR: unknown ticket " + ticketId;
        }
        if (!(ticket instanceof IndividualTicket)) {
            return "ERROR: passes are attached to individual tickets, not to group ticket " + ticketId;
        }
        return "ERROR: ticket " + ticketId + " is dated before " + parkController.getCurrentDate();
    }

    private static int passCount(IndividualTicket ticket) {
        return ticket.getSpeedPasses().size() + (ticket.getSpecialPass() == null ? 0 : 1);
    }

    /**
     * Finds any ticket by id: individual, group, or generated inside a group ticket.
     */
    public Ticket findTicket(String id) {
        for (IndividualTicket ticket : allIndividualTickets()) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return findGroupTicket(id);
    }

    private GroupTicket findGroupTicket(String id) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getId().equals(id)) {
                return groupTicket;
            }
        }
        return null;
    }

    /**
     * All individual tickets: bought directly and generated by group tickets.
     */
    public List<IndividualTicket> allIndividualTickets() {
        List<IndividualTicket> all = new ArrayList<>(tickets);
        for (GroupTicket groupTicket : groupTickets) {
            all.addAll(groupTicket.getTickets());
        }
        return all;
    }

    /**
     * A ticket of the user dated on or after the system date, or null.
     */
    public IndividualTicket findActiveTicket(User user) {
        for (IndividualTicket ticket : allIndividualTickets()) {
            if (ticket.getUser() == user && ticket.isActiveOn(parkController.getCurrentDate())) {
                return ticket;
            }
        }
        return null;
    }

    public boolean hasActiveGroupTicket(Group group) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getGroup() == group && groupTicket.isActiveOn(parkController.getCurrentDate())) {
                return true;
            }
        }
        return false;
    }

    private String checkNewTicket(String id, String dateText) {
        if (!Validator.isValidId(id)) {
            return "ERROR: invalid ticket id";
        }
        if (findTicket(id) != null) {
            return "ERROR: ticket id " + id + " already exists";
        }
        LocalDate date = Validator.parseDate(dateText);
        if (date == null) {
            return "ERROR: invalid date, expected yyyy-MM-dd";
        }
        LocalDate today = parkController.getCurrentDate();
        if (date.isBefore(today)) {
            return "ERROR: tickets cannot be bought for a date before " + today;
        }
        return null;
    }

    private boolean saveTickets() {
        try {
            ticketRepository.save(tickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean savePasses() {
        try {
            passRepository.save(allIndividualTickets());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Best-effort rewrite of every ticket file from the in-memory state after a failed write.
     */
    private void restoreFiles() {
        saveTickets();
        saveGroupTickets();
        savePasses();
    }

    private boolean saveGroupTickets() {
        try {
            groupTicketRepository.save(groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
