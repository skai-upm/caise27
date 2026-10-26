package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.Values;
import es.upm.parque.business.validation.UserValidator;
import es.upm.parque.persistence.TicketRepository;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for individual and group tickets. Dates are always compared with the system date
 * held by the {@link MainController}. Every successful change is persisted.
 */
public final class TicketController {

    private TicketRepository repository;
    private UserController userController;
    private GroupController groupController;
    private MainController mainController;
    private List<IndividualTicket> individualTickets;
    private List<GroupTicket> groupTickets;

    private TicketController() {
    }

    /**
     * Returns the single instance of this controller.
     */
    public static TicketController getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Injects the repository and collaborating controllers chosen at startup and loads the stored data.
     */
    public void configure(TicketRepository repository, UserController userController, GroupController groupController,
                         MainController mainController) {
        this.repository = repository;
        this.userController = userController;
        this.groupController = groupController;
        this.mainController = mainController;
        load();
    }

    /**
     * Every ticket, individual (including those inside group tickets) and group.
     */
    public List<Ticket> getTickets() {
        requireConfigured();
        List<Ticket> all = new ArrayList<>(individualTickets);
        all.addAll(groupTickets);
        return all;
    }

    public List<IndividualTicket> getIndividualTickets() {
        requireConfigured();
        return List.copyOf(individualTickets);
    }

    public List<GroupTicket> getGroupTickets() {
        requireConfigured();
        return List.copyOf(groupTickets);
    }

    /**
     * Individual tickets bought directly for the user, excluding those inside group tickets.
     */
    public List<IndividualTicket> getStandaloneTicketsOf(User user) {
        requireConfigured();
        return individualTickets.stream().filter(t -> t.getUser() == user && ownerOf(t) == null).toList();
    }

    public List<GroupTicket> getGroupTicketsOf(Group group) {
        requireConfigured();
        return groupTickets.stream().filter(t -> t.getGroup() == group).toList();
    }

    /**
     * Every individual ticket of the user, standalone or inside a group ticket.
     */
    public List<IndividualTicket> getIndividualTicketsOf(User user) {
        requireConfigured();
        return individualTickets.stream().filter(t -> t.getUser() == user).toList();
    }

    /**
     * The individual ticket with that identifier, or {@code null} if there is none.
     */
    public IndividualTicket findIndividualTicket(String id) {
        requireConfigured();
        return individualTickets.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Persists changes made to tickets or their passes by other controllers.
     */
    public void save() {
        requireConfigured();
        persist();
    }

    public Ticket findById(String id) {
        requireConfigured();
        return getTickets().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Buys an individual ticket for the user with the given email at the current price.
     */
    public IndividualTicket create(String id, String email, String dateText) {
        requireConfigured();
        requireNewId(id);
        User user = userController.findByEmail(email);
        if (user == null) {
            throw new ParkException("No user with email " + email);
        }
        LocalDate date = parseFutureOrToday(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, mainController.getTicketPrice());
        individualTickets.add(ticket);
        persist();
        return ticket;
    }

    /**
     * Buys a group ticket for the current members of a group, generating one individual ticket each.
     */
    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        requireConfigured();
        requireNewId(id);
        Group group = groupController.findById(groupId);
        if (group == null) {
            throw new ParkException("No group with identifier " + groupId);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new ParkException("Group " + groupId + " has no members");
        }
        LocalDate date = parseFutureOrToday(dateText);
        BigDecimal unitPrice = mainController.getTicketPrice();
        List<IndividualTicket> internal = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            String internalId = id + "-" + (i + 1);
            if (findById(internalId) != null) {
                throw new ParkException("A ticket with identifier " + internalId + " already exists");
            }
            internal.add(new IndividualTicket(internalId, date, members.get(i), unitPrice));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, GroupTicket.totalFor(unitPrice, members.size()), internal);
        individualTickets.addAll(internal);
        groupTickets.add(ticket);
        persist();
        return ticket;
    }

    /**
     * Deletes a standalone individual ticket whose date is strictly later than the system date,
     * together with its passes. If saving fails, the stored state is reloaded unchanged.
     */
    public void delete(String id) {
        requireConfigured();
        IndividualTicket ticket = individualTickets.stream().filter(t -> t.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ParkException("No individual ticket with identifier " + id));
        GroupTicket owner = ownerOf(ticket);
        if (owner != null) {
            throw new ParkException("Ticket " + id + " belongs to group ticket " + owner.getId()
                    + "; delete the group ticket instead");
        }
        requireStrictlyFuture(ticket);
        ticket.clearPasses();
        individualTickets.remove(ticket);
        persist();
    }

    /**
     * Deletes a group ticket whose date is strictly later than the system date: first the passes of
     * its internal tickets, then those tickets, then the group ticket. If saving fails, the stored
     * state is reloaded unchanged.
     */
    public void deleteGroupTicket(String id) {
        requireConfigured();
        GroupTicket ticket = groupTickets.stream().filter(t -> t.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ParkException("No group ticket with identifier " + id));
        requireStrictlyFuture(ticket);
        ticket.getTickets().forEach(IndividualTicket::clearPasses);
        individualTickets.removeAll(ticket.getTickets());
        groupTickets.remove(ticket);
        persist();
    }

    /**
     * True if the user has an individual ticket (standalone or internal) dated on or after the system date.
     */
    public boolean hasCurrentOrFutureTicket(User user) {
        requireConfigured();
        LocalDate today = mainController.getCurrentDate();
        return individualTickets.stream().anyMatch(t -> t.getUser() == user && !t.getDate().isBefore(today));
    }

    /**
     * True if the group has a group ticket dated on or after the system date.
     */
    public boolean isGroupLocked(Group group) {
        requireConfigured();
        LocalDate today = mainController.getCurrentDate();
        return groupTickets.stream().anyMatch(t -> t.getGroup() == group && !t.getDate().isBefore(today));
    }

    /**
     * Removes every ticket of a user that is being deleted, including from group ticket compositions.
     */
    public void removeTicketsOf(User user) {
        requireConfigured();
        List<IndividualTicket> owned = individualTickets.stream().filter(t -> t.getUser() == user).toList();
        if (owned.isEmpty()) {
            return;
        }
        for (IndividualTicket t : owned) {
            GroupTicket owner = ownerOf(t);
            if (owner != null) {
                owner.removeTicket(t);
            }
        }
        individualTickets.removeAll(owned);
        persist();
    }

    /**
     * Removes the group tickets (and their internal tickets) of a group that is being deleted.
     */
    public void removeGroupTicketsOf(Group group) {
        requireConfigured();
        List<GroupTicket> owned = groupTickets.stream().filter(t -> t.getGroup() == group).toList();
        if (owned.isEmpty()) {
            return;
        }
        for (GroupTicket t : owned) {
            individualTickets.removeAll(t.getTickets());
        }
        groupTickets.removeAll(owned);
        persist();
    }

    private GroupTicket ownerOf(IndividualTicket ticket) {
        return groupTickets.stream().filter(g -> g.contains(ticket)).findFirst().orElse(null);
    }

    private void requireNewId(String id) {
        if (!UserValidator.isNotBlank(id)) {
            throw new ParkException("The ticket identifier is required");
        }
        if (findById(id) != null) {
            throw new ParkException("A ticket with identifier " + id + " already exists");
        }
    }

    private LocalDate parseFutureOrToday(String dateText) {
        LocalDate date = Values.parseDate(dateText);
        if (date.isBefore(mainController.getCurrentDate())) {
            throw new ParkException("Cannot buy a ticket for a past date: " + dateText);
        }
        return date;
    }

    private void requireStrictlyFuture(Ticket ticket) {
        if (!ticket.getDate().isAfter(mainController.getCurrentDate())) {
            throw new ParkException("Only tickets dated after the system date can be deleted");
        }
    }

    private void load() {
        Map<String, User> usersByDni = new HashMap<>();
        for (User u : userController.getUsers()) {
            usersByDni.put(u.getDni(), u);
        }
        Map<String, Group> groupsById = new HashMap<>();
        for (Group g : groupController.getGroups()) {
            groupsById.put(g.getId(), g);
        }
        TicketRepository.TicketData data = repository.load(usersByDni, groupsById);
        individualTickets = new ArrayList<>(data.individual());
        groupTickets = new ArrayList<>(data.group());
    }

    private void persist() {
        try {
            repository.save(individualTickets, groupTickets);
        } catch (UncheckedIOException e) {
            load();
            throw new ParkException("Could not save tickets: " + e.getMessage());
        }
    }

    private void requireConfigured() {
        if (repository == null) {
            throw new IllegalStateException("TicketController is not configured");
        }
    }

    private static final class Holder {
        static final TicketController INSTANCE = new TicketController();
    }
}
