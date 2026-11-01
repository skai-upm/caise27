package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.OperationException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.StorageConfig;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;

/**
 * Main controller of the park. It owns the single system date and base ticket
 * price used by every rule, the users, groups, tickets and attractions, and
 * persists each successful change. Rejected operations leave state unchanged.
 */
public class Park {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal basePrice = DEFAULT_PRICE;

    private final UserManager userManager;
    private final GroupRepository groupRepository;
    private final TicketRepository ticketRepository;
    private final AttractionRepository attractionRepository;
    private final List<Group> groups;
    private final List<Ticket> tickets;
    private final List<Attraction> attractions;

    /** Loads the park from storage, starting on the operating system's current date. */
    public Park(StorageConfig storage) throws IOException {
        this(storage, LocalDate.now());
    }

    public Park(StorageConfig storage, LocalDate startDate) throws IOException {
        this.currentDate = startDate;
        this.userManager = new UserManager(new UserRepository(storage.users()));
        this.groupRepository = new GroupRepository(storage.groups());
        this.ticketRepository = new TicketRepository(storage.tickets());
        this.attractionRepository = new AttractionRepository(storage.attractions());

        Map<String, User> usersByDni = new HashMap<>();
        for (User user : userManager.getUsers()) {
            usersByDni.put(user.getDni(), user);
        }
        this.groups = new ArrayList<>(groupRepository.load(usersByDni));
        Map<String, Group> groupsById = new HashMap<>();
        for (Group group : groups) {
            groupsById.put(group.getId(), group);
        }
        this.tickets = new ArrayList<>(ticketRepository.load(usersByDni, groupsById));
        this.attractions = new ArrayList<>(attractionRepository.load());
    }

    public UserManager getUserManager() {
        return userManager;
    }

    // ---- system settings ----

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) {
        if (price.signum() <= 0) {
            throw new OperationException("price must be greater than zero");
        }
        this.basePrice = price.setScale(2, RoundingMode.HALF_UP);
    }

    // ---- users ----

    /** Deletes an authenticated user who holds no current or future ticket. */
    public void deleteUser(String email, String password) {
        User user = userManager.authenticate(email, password);
        for (Ticket ticket : tickets) {
            if (ticket.isActiveOn(currentDate) && ticket.getUsers().contains(user)) {
                throw new OperationException("user " + email + " has ticket " + ticket.getId()
                        + " dated " + ticket.getDate() + " and cannot be deleted");
            }
        }
        // Refresh the holder data kept with historical tickets before the user disappears.
        save(() -> ticketRepository.save(tickets), () -> { });
        List<Group> memberOf = new ArrayList<>();
        for (Group group : groups) {
            if (group.hasMember(user)) {
                memberOf.add(group);
                group.removeMember(user);
            }
        }
        Runnable restoreGroups = () -> memberOf.forEach(group -> group.addMember(user));
        save(() -> groupRepository.save(groups), restoreGroups);
        try {
            userManager.remove(user);
        } catch (OperationException e) {
            restoreGroups.run();
            save(() -> groupRepository.save(groups), () -> { });
            throw e;
        }
        for (Attraction attraction : attractions) {
            attraction.removeVisitor(user);
        }
    }

    // ---- groups ----

    public Group findGroup(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public void createGroup(String id, String name) {
        if (findGroup(id) != null) {
            throw new OperationException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        save(() -> groupRepository.save(groups), () -> groups.remove(group));
    }

    public void addMember(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        User user = existingUser(email);
        if (group.hasMember(user)) {
            throw new OperationException("user " + email + " is already in group " + groupId);
        }
        group.addMember(user);
        save(() -> groupRepository.save(groups), () -> group.removeMember(user));
    }

    public void removeMember(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        User user = userManager.findByEmail(email);
        if (user == null || !group.hasMember(user)) {
            throw new OperationException("user " + email + " is not in group " + groupId);
        }
        List<User> before = new ArrayList<>(group.getMembers());
        group.removeMember(user);
        save(() -> groupRepository.save(groups), () -> {
            before.forEach(group::removeMember);
            before.forEach(group::addMember);
        });
    }

    public void deleteGroup(String groupId) {
        Group group = unlockedGroup(groupId);
        int index = groups.indexOf(group);
        groups.remove(index);
        save(() -> groupRepository.save(groups), () -> groups.add(index, group));
    }

    /** A group is locked while it has a group ticket dated on or after the system date. */
    public boolean isLocked(Group group) {
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    private Group existingGroup(String groupId) {
        Group group = findGroup(groupId);
        if (group == null) {
            throw new OperationException("group " + groupId + " does not exist");
        }
        return group;
    }

    private Group unlockedGroup(String groupId) {
        Group group = existingGroup(groupId);
        if (isLocked(group)) {
            throw new OperationException("group " + groupId + " has a current or future group ticket and is locked");
        }
        return group;
    }

    private User existingUser(String email) {
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new OperationException("user " + email + " does not exist");
        }
        return user;
    }

    // ---- tickets ----

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    public Ticket findTicket(String id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    private boolean isTicketIdUsed(String id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return true;
            }
            if (ticket instanceof GroupTicket groupTicket) {
                for (IndividualTicket constituent : groupTicket.getTickets()) {
                    if (constituent.getId().equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void requirePurchasable(String id, LocalDate date) {
        if (isTicketIdUsed(id)) {
            throw new OperationException("ticket " + id + " already exists");
        }
        if (date.isBefore(currentDate)) {
            throw new OperationException("cannot buy a ticket for " + date + ", before the system date " + currentDate);
        }
    }

    public IndividualTicket createTicket(String id, String email, LocalDate date) {
        User user = existingUser(email);
        requirePurchasable(id, date);
        IndividualTicket ticket = new IndividualTicket(id, date, basePrice, user);
        tickets.add(ticket);
        save(() -> ticketRepository.save(tickets), () -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new OperationException(isTicketIdUsed(id)
                    ? "ticket " + id + " belongs to a group ticket; use GROUPTICKET DELETE"
                    : "ticket " + id + " does not exist");
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new OperationException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        removeRefundable(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        Group group = existingGroup(groupId);
        int members = group.getMembers().size();
        if (members == 0) {
            throw new OperationException("group " + groupId + " has no members");
        }
        requirePurchasable(id, date);
        for (int position = 1; position <= members; position++) {
            String constituentId = GroupTicket.constituentId(id, position);
            if (isTicketIdUsed(constituentId)) {
                throw new OperationException("ticket " + constituentId + " already exists");
            }
        }
        GroupTicket ticket = new GroupTicket(id, group, date, basePrice);
        tickets.add(ticket);
        save(() -> ticketRepository.save(tickets), () -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = findTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationException("group ticket " + id + " does not exist");
        }
        removeRefundable(ticket);
    }

    private void removeRefundable(Ticket ticket) {
        if (!ticket.isRefundableOn(currentDate)) {
            throw new OperationException("ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + "; only tickets after the system date " + currentDate + " can be deleted");
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        save(() -> ticketRepository.save(tickets), () -> tickets.add(index, ticket));
    }

    // ---- attractions ----

    public Attraction findAttraction(String id) {
        for (Attraction attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    public void createAttraction(String id, String name, int capacity) {
        if (findAttraction(id) != null) {
            throw new OperationException("attraction " + id + " already exists");
        }
        if (capacity <= 0) {
            throw new OperationException("capacity must be a positive integer");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        save(() -> attractionRepository.save(attractions), () -> attractions.remove(attraction));
    }

    /** Admits the users of a ticket valid for the system date; returns how many entered. */
    public int enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = existingAttraction(attractionId);
        Ticket ticket = existingTicket(ticketId);
        if (!ticket.getDate().equals(currentDate)) {
            throw new OperationException("ticket " + ticketId + " is valid for " + ticket.getDate()
                    + ", not for the system date " + currentDate);
        }
        attraction.enter(ticket);
        return ticket.getUsers().size();
    }

    /** Lets the present users of a ticket leave, without checking the date; returns how many left. */
    public int exitAttraction(String attractionId, String ticketId) {
        return existingAttraction(attractionId).exit(existingTicket(ticketId));
    }

    private Attraction existingAttraction(String id) {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            throw new OperationException("attraction " + id + " does not exist");
        }
        return attraction;
    }

    private Ticket existingTicket(String id) {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new OperationException("ticket " + id + " does not exist");
        }
        return ticket;
    }

    // ---- persistence ----

    @FunctionalInterface
    private interface SaveAction {
        void run() throws IOException;
    }

    private static void save(SaveAction action, Runnable undo) {
        try {
            action.run();
        } catch (IOException e) {
            undo.run();
            throw new OperationException("could not save data: " + e.getMessage());
        }
    }
}
