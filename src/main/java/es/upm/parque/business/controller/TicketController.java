package es.upm.parque.business.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.validation.UserValidator;
import es.upm.parque.persistence.TicketRepository;

/**
 * Manages individual and group tickets (Singleton). Internal tickets of a group ticket live inside it, and
 * passes live inside their individual ticket, so deleting a ticket removes everything it owns.
 */
public final class TicketController {

    private static TicketController instance;

    private final List<IndividualTicket> individualTickets = new ArrayList<>();
    private final List<GroupTicket> groupTickets = new ArrayList<>();
    private TicketRepository repository;
    private ParkController park;
    private UserController users;
    private GroupController groups;

    private TicketController() {
    }

    public static synchronized TicketController getInstance() {
        if (instance == null) {
            instance = new TicketController();
        }
        return instance;
    }

    public void configure(TicketRepository repository, ParkController park, UserController users,
            GroupController groups) {
        this.repository = repository;
        this.park = park;
        this.users = users;
        this.groups = groups;
    }

    /**
     * Loads tickets and their passes; users and groups must be loaded first.
     */
    public void load() throws IOException {
        TicketRepository.Loaded loaded = repository.load(users::findByDni, groups::find);
        individualTickets.clear();
        individualTickets.addAll(loaded.individualTickets());
        groupTickets.clear();
        groupTickets.addAll(loaded.groupTickets());
    }

    public List<IndividualTicket> getIndividualTickets() {
        return List.copyOf(individualTickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    /**
     * Every individual ticket, whether bought individually or internal to a group ticket.
     */
    public List<IndividualTicket> getAllIndividualTickets() {
        List<IndividualTicket> all = new ArrayList<>(individualTickets);
        for (GroupTicket groupTicket : groupTickets) {
            all.addAll(groupTicket.getTickets());
        }
        return all;
    }

    /**
     * Finds an individual ticket, an internal ticket of a group ticket or a group ticket by identifier.
     */
    public Ticket find(String id) {
        for (IndividualTicket ticket : getAllIndividualTickets()) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getId().equals(id)) {
                return groupTicket;
            }
        }
        return null;
    }

    public boolean isIdTaken(String id) {
        return find(id) != null;
    }

    /**
     * Returns the group ticket that contains the given ticket, or {@code null} for an individually bought one.
     */
    public GroupTicket groupTicketOf(IndividualTicket ticket) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    public String createTicket(String id, String email, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR Unknown user: " + email;
        }
        IndividualTicket ticket = new IndividualTicket(id, ParkController.parseDate(dateText), user,
                park.getTicketPrice());
        individualTickets.add(ticket);
        if (!save()) {
            individualTickets.remove(ticket);
            return "ERROR Could not save tickets";
        }
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    /**
     * Deletes a future individual ticket together with its passes.
     */
    public String deleteTicket(String id) {
        if (!(find(id) instanceof IndividualTicket ticket)) {
            return "ERROR Unknown ticket: " + id;
        }
        GroupTicket owner = groupTicketOf(ticket);
        if (owner != null) {
            return "ERROR Ticket " + id + " belongs to group ticket " + owner.getId()
                    + "; delete the group ticket instead";
        }
        if (!ticket.isFuture(park.getCurrentDate())) {
            return "ERROR Only tickets dated after " + park.getCurrentDate() + " can be deleted";
        }
        int index = individualTickets.indexOf(ticket);
        individualTickets.remove(index);
        if (!save()) {
            individualTickets.add(index, ticket);
            save();
            return "ERROR Could not save tickets";
        }
        return "OK TICKET DELETED " + id;
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        Group group = groups.find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        if (group.getMembers().isEmpty()) {
            return "ERROR Group " + groupId + " has no members";
        }
        for (User member : group.getMembers()) {
            String internalId = GroupTicket.internalTicketId(id, member);
            if (isIdTaken(internalId)) {
                return "ERROR Ticket " + internalId + " already exists";
            }
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, ParkController.parseDate(dateText), group,
                park.getTicketPrice());
        groupTickets.add(groupTicket);
        if (!save()) {
            groupTickets.remove(groupTicket);
            return "ERROR Could not save tickets";
        }
        return "OK GROUP TICKET CREATED -> " + groupTicket.getPrice().toPlainString();
    }

    /**
     * Deletes a future group ticket: the passes of its internal tickets, the internal tickets and the group
     * ticket are removed together, or nothing is removed.
     */
    public String deleteGroupTicket(String id) {
        if (!(find(id) instanceof GroupTicket groupTicket)) {
            return "ERROR Unknown group ticket: " + id;
        }
        if (!groupTicket.isFuture(park.getCurrentDate())) {
            return "ERROR Only group tickets dated after " + park.getCurrentDate() + " can be deleted";
        }
        int index = groupTickets.indexOf(groupTicket);
        groupTickets.remove(index);
        if (!save()) {
            groupTickets.add(index, groupTicket);
            save();
            return "ERROR Could not save tickets";
        }
        return "OK GROUP TICKET DELETED " + id;
    }

    private String checkNewTicket(String id, String dateText) {
        if (!UserValidator.isNotBlank(id)) {
            return "ERROR Ticket identifier must not be empty";
        }
        if (isIdTaken(id)) {
            return "ERROR Ticket " + id + " already exists";
        }
        LocalDate date = ParkController.parseDate(dateText);
        if (date == null) {
            return "ERROR Invalid date: " + dateText + " (expected yyyy-MM-dd)";
        }
        if (date.isBefore(park.getCurrentDate())) {
            return "ERROR Tickets cannot be bought for a date before " + park.getCurrentDate();
        }
        return null;
    }

    /**
     * True if the user holds any ticket, individual or internal, dated on or after {@code today}.
     */
    public boolean hasCurrentOrFutureTicket(User user, LocalDate today) {
        for (IndividualTicket ticket : ticketsOf(user)) {
            if (ticket.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the group has any group ticket dated on or after {@code today}.
     */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getGroup() == group && groupTicket.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Every ticket held by the user, including internal tickets of group tickets.
     */
    public List<IndividualTicket> ticketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket ticket : getAllIndividualTickets()) {
            if (ticket.getUser() == user) {
                result.add(ticket);
            }
        }
        return result;
    }

    /**
     * Tickets the user bought individually (not internal tickets of group tickets).
     */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket ticket : individualTickets) {
            if (ticket.getUser() == user) {
                result.add(ticket);
            }
        }
        return result;
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    /**
     * Deletes every ticket of the user (with its passes), removing internal ones from their group tickets.
     */
    public boolean removeTicketsOf(User user) {
        individualTickets.removeIf(ticket -> ticket.getUser() == user);
        for (GroupTicket groupTicket : groupTickets) {
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                if (ticket.getUser() == user) {
                    groupTicket.removeTicket(ticket);
                }
            }
        }
        return save();
    }

    /**
     * Deletes every group ticket of the group together with its internal tickets and their passes.
     */
    public boolean removeGroupTicketsOf(Group group) {
        groupTickets.removeIf(groupTicket -> groupTicket.getGroup() == group);
        return save();
    }

    /**
     * Writes the current tickets and passes to the repository.
     */
    public boolean save() {
        try {
            repository.saveAll(individualTickets, groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
