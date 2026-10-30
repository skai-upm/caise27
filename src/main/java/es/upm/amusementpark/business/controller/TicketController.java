package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Formats;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Individual and group ticket operations with persistence. Ticket identifiers share one namespace that
 * includes the constituent tickets of group tickets.
 */
public class TicketController {

    private static TicketController instance;

    private static final String SAVE_ERROR = "ERROR: could not save tickets";

    private TicketRepository repository;
    private PassRepository passRepository;
    private List<IndividualTicket> tickets = new ArrayList<>();
    private List<GroupTicket> groupTickets = new ArrayList<>();

    private TicketController() {
    }

    /** Returns the unique instance of this controller. */
    public static synchronized TicketController getInstance() {
        if (instance == null) {
            instance = new TicketController();
        }
        return instance;
    }

    /**
     * Injects the ticket and pass repositories and loads stored tickets (resolving users and groups through their
     * controllers) and then their passes.
     */
    public void configure(TicketRepository repository, PassRepository passRepository, UserController userController,
            GroupController groupController) throws IOException {
        List<User> users = userController.getUsers();
        List<Group> groups = groupController.getGroups();
        this.repository = repository;
        this.passRepository = passRepository;
        this.tickets = new ArrayList<>(repository.loadTickets(users));
        this.groupTickets = new ArrayList<>(repository.loadGroupTickets(users, groups));
        passRepository.load(getAllIndividualTickets());
    }

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    /** Standalone individual tickets plus the constituent tickets of every group ticket. */
    public List<IndividualTicket> getAllIndividualTickets() {
        List<IndividualTicket> all = new ArrayList<>(tickets);
        for (GroupTicket gt : groupTickets) {
            all.addAll(gt.getTickets());
        }
        return all;
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    /** Finds any ticket by id: standalone individual, group, or constituent of a group ticket. */
    public Ticket findById(String id) {
        for (IndividualTicket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        for (GroupTicket gt : groupTickets) {
            if (gt.getId().equals(id)) {
                return gt;
            }
            for (IndividualTicket t : gt.getTickets()) {
                if (t.getId().equals(id)) {
                    return t;
                }
            }
        }
        return null;
    }

    public String createIndividual(String id, User user, LocalDate date, BigDecimal price, LocalDate today) {
        String error = checkNewTicket(id, date, today);
        if (error != null) {
            return error;
        }
        IndividualTicket ticket = new IndividualTicket(id, date, user, price);
        tickets.add(ticket);
        if (!persist()) {
            tickets.remove(ticket);
            return SAVE_ERROR;
        }
        return "OK TICKET CREATED -> " + Formats.formatPrice(price);
    }

    public String createGroup(String id, Group group, LocalDate date, BigDecimal basePrice, LocalDate today) {
        String error = checkNewTicket(id, date, today);
        if (error != null) {
            return error;
        }
        if (group.getMembers().isEmpty()) {
            return "ERROR: group " + group.getId() + " has no members";
        }
        GroupTicket ticket = GroupTicket.purchase(id, date, group, basePrice);
        for (IndividualTicket t : ticket.getTickets()) {
            if (findById(t.getId()) != null) {
                return "ERROR: ticket identifier " + t.getId() + " is already in use";
            }
        }
        groupTickets.add(ticket);
        if (!persist()) {
            groupTickets.remove(ticket);
            return SAVE_ERROR;
        }
        return "OK GROUP TICKET CREATED -> " + Formats.formatPrice(ticket.getPrice());
    }

    public String deleteIndividual(String id, LocalDate today) {
        IndividualTicket ticket = null;
        for (IndividualTicket t : tickets) {
            if (t.getId().equals(id)) {
                ticket = t;
            }
        }
        if (ticket == null) {
            return findById(id) != null
                    ? "ERROR: ticket " + id + " belongs to a group ticket; use GROUPTICKET DELETE"
                    : "ERROR: ticket " + id + " does not exist";
        }
        if (!ticket.isRefundableOn(today)) {
            return "ERROR: ticket " + id + " can only be deleted before its date";
        }
        int index = tickets.indexOf(ticket);
        int passes = ticket.getPasses().size();
        tickets.remove(index);
        if (!persist()) {
            tickets.add(index, ticket);
            persist();
            return SAVE_ERROR;
        }
        return "OK: ticket " + id + " deleted with its " + passes + " pass(es)";
    }

    public String deleteGroup(String id, LocalDate today) {
        GroupTicket ticket = null;
        for (GroupTicket gt : groupTickets) {
            if (gt.getId().equals(id)) {
                ticket = gt;
            }
        }
        if (ticket == null) {
            return "ERROR: group ticket " + id + " does not exist";
        }
        if (!ticket.isRefundableOn(today)) {
            return "ERROR: group ticket " + id + " can only be deleted before its date";
        }
        int index = groupTickets.indexOf(ticket);
        int passes = 0;
        for (IndividualTicket t : ticket.getTickets()) {
            passes += t.getPasses().size();
        }
        groupTickets.remove(index);
        if (!persist()) {
            groupTickets.add(index, ticket);
            persist();
            return SAVE_ERROR;
        }
        return "OK: group ticket " + id + " deleted with its " + ticket.getTickets().size() + " tickets and " + passes
                + " pass(es)";
    }

    /** True if the user holds any ticket (standalone or in a group ticket) dated on or after {@code today}. */
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (IndividualTicket t : tickets) {
            if (t.getUser() == user && t.isActiveOn(today)) {
                return true;
            }
        }
        for (GroupTicket gt : groupTickets) {
            if (gt.isActiveOn(today) && gt.getUsers().contains(user)) {
                return true;
            }
        }
        return false;
    }

    /** True if the group has a group ticket dated on or after {@code today}. */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (GroupTicket gt : groupTickets) {
            if (gt.getGroup() == group && gt.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    private String checkNewTicket(String id, LocalDate date, LocalDate today) {
        if (!UserValidator.isNotBlank(id)) {
            return "ERROR: ticket identifier must not be empty";
        }
        if (findById(id) != null) {
            return "ERROR: ticket identifier " + id + " is already in use";
        }
        if (date.isBefore(today)) {
            return "ERROR: tickets cannot be bought for a past date";
        }
        return null;
    }

    /**
     * Saves passes first and then tickets and group tickets, so files never reference passes of deleted tickets.
     *
     * @return {@code false} if any file could not be written
     */
    public boolean persist() {
        try {
            passRepository.save(getAllIndividualTickets());
            repository.save(tickets, groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
