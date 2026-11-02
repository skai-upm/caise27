package es.upm.etsisi.amusementpark.park;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.persistence.ParkData;
import es.upm.etsisi.amusementpark.persistence.ParkStorage;
import es.upm.etsisi.amusementpark.persistence.UserRepository;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.ticket.Ticket;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserException;
import es.upm.etsisi.amusementpark.user.UserField;
import es.upm.etsisi.amusementpark.user.UserService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Central park management. Exactly one instance exists per execution; it coordinates users,
 * groups, tickets and attractions, and holds the current system date and base price.
 *
 * <p>Every operation validates first. State changes are persisted through {@link ParkStorage};
 * if saving fails the in-memory change is undone, so rejected operations change nothing.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private ParkStorage storage;
    private UserService users;
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Attraction> attractions = new ArrayList<>();
    private LocalDate currentDate;
    private BigDecimal basePrice;

    private Park() {
    }

    private static final class Holder {
        private static final Park INSTANCE = new Park();
    }

    public static Park getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Loads the park state from the given storage and resets the date and price for a new execution.
     */
    public void initialize(ParkStorage parkStorage, LocalDate startDate) throws IOException {
        ParkData data = parkStorage.load();
        storage = parkStorage;
        groups.clear();
        groups.addAll(data.groups());
        tickets.clear();
        tickets.addAll(data.tickets());
        attractions.clear();
        attractions.addAll(data.attractions());
        users = new UserService(new UserRepository() {
            @Override
            public List<User> loadAll() {
                return data.users();
            }

            @Override
            public void saveAll(List<User> userList) throws IOException {
                storage.save(userList, groups, tickets, attractions);
            }
        });
        currentDate = startDate;
        basePrice = INITIAL_PRICE;
    }

    // ---- system settings -------------------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) throws ParkException {
        if (price == null || price.signum() <= 0) {
            throw new ParkException("price must be a positive amount");
        }
        basePrice = price;
    }

    // ---- users -----------------------------------------------------------------------

    public List<User> getUsers() {
        return users.getUsers();
    }

    public void createUser(User user) throws UserException {
        users.create(user);
    }

    public void updateUser(String email, String password, UserField field, String value) throws UserException {
        users.update(email, password, field, value);
    }

    /**
     * Deletes a user unless they hold an active ticket or belong to a locked group; on success
     * the user also leaves their groups and attractions. Historical tickets are kept.
     */
    public void deleteUser(String email, String password) throws UserException {
        User user = users.findByEmail(email).orElseThrow(() -> new ParkException("user not found"));
        if (!user.hasPassword(password)) {
            throw new ParkException("incorrect password");
        }
        if (allIndividualTickets().anyMatch(t -> t.getUser() == user && t.isActiveOn(currentDate))) {
            throw new ParkException("user has active tickets");
        }
        List<Group> memberships = groups.stream().filter(g -> g.hasMember(user)).toList();
        if (memberships.stream().anyMatch(this::isLocked)) {
            throw new ParkException("user belongs to a group with an active group ticket");
        }
        Map<Group, Integer> positions = new LinkedHashMap<>();
        memberships.forEach(g -> positions.put(g, g.removeMember(user)));
        List<Attraction> inside = attractions.stream().filter(a -> a.release(user)).toList();
        try {
            users.delete(email, password);
        } catch (UserException e) {
            positions.forEach((g, index) -> g.insertMember(index, user));
            inside.forEach(a -> a.admit(List.of(user)));
            throw e;
        }
    }

    // ---- groups ----------------------------------------------------------------------

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Optional<Group> findGroup(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    /** A group is locked while any of its group tickets is active; this is never stored. */
    public boolean isLocked(Group group) {
        return tickets.stream().anyMatch(t -> t instanceof GroupTicket gt
                && gt.getGroup() == group && gt.isActiveOn(currentDate));
    }

    public void createGroup(String id, String name) throws ParkException {
        requireText(id, "group identifier");
        requireText(name, "group name");
        if (findGroup(id).isPresent()) {
            throw new ParkException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        persist(() -> groups.remove(group));
    }

    public void addToGroup(String groupId, String email) throws ParkException {
        Group group = requireModifiableGroup(groupId);
        User user = requireUser(email);
        if (group.hasMember(user)) {
            throw new ParkException("user already belongs to group " + groupId);
        }
        group.addMember(user);
        persist(() -> group.removeMember(user));
    }

    public void removeFromGroup(String groupId, String email) throws ParkException {
        Group group = requireModifiableGroup(groupId);
        User user = requireUser(email);
        int index = group.removeMember(user);
        if (index < 0) {
            throw new ParkException("user does not belong to group " + groupId);
        }
        persist(() -> group.insertMember(index, user));
    }

    public void deleteGroup(String groupId) throws ParkException {
        Group group = requireModifiableGroup(groupId);
        int index = groups.indexOf(group);
        List<GroupTicket> history = tickets.stream()
                .filter(t -> t instanceof GroupTicket gt && gt.getGroup() == group)
                .map(GroupTicket.class::cast).toList();
        groups.remove(index);
        history.forEach(t -> t.setGroup(null));
        persist(() -> {
            groups.add(index, group);
            history.forEach(t -> t.setGroup(group));
        });
    }

    // ---- tickets ---------------------------------------------------------------------

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    /** Finds any ticket in the shared identifier space, including group ticket constituents. */
    public Optional<Ticket> findTicket(String id) {
        return Stream.concat(tickets.stream(), allIndividualTickets().map(Ticket.class::cast))
                .filter(t -> t.getId().equals(id)).findFirst();
    }

    public IndividualTicket createTicket(String id, String email, LocalDate date) throws ParkException {
        requireText(id, "ticket identifier");
        User user = requireUser(email);
        requireNotPast(date);
        requireUnusedIds(List.of(id));
        IndividualTicket ticket = new IndividualTicket(id, date, user, basePrice, 0);
        tickets.add(ticket);
        persist(() -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = findTicket(id).orElseThrow(() -> new ParkException("ticket " + id + " not found"));
        if (!tickets.contains(ticket)) {
            throw new ParkException("ticket " + id + " belongs to a group ticket");
        }
        if (ticket instanceof GroupTicket) {
            throw new ParkException("ticket " + id + " is a group ticket, use GROUPTICKET DELETE");
        }
        removeFutureTicket(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws ParkException {
        requireText(id, "ticket identifier");
        Group group = requireGroup(groupId);
        requireNotPast(date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new ParkException("group " + groupId + " has no members");
        }
        List<String> ids = new ArrayList<>();
        ids.add(id);
        members.forEach(m -> ids.add(GroupTicket.constituentId(id, m.getDni())));
        requireUnusedIds(ids);
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = members.stream()
                .map(m -> new IndividualTicket(GroupTicket.constituentId(id, m.getDni()), date, m, basePrice, discount))
                .toList();
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        tickets.add(ticket);
        persist(() -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = tickets.stream().filter(t -> t instanceof GroupTicket && t.getId().equals(id))
                .findFirst().orElseThrow(() -> new ParkException("group ticket " + id + " not found"));
        removeFutureTicket(ticket);
    }

    // ---- attractions -----------------------------------------------------------------

    public List<Attraction> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Optional<Attraction> findAttraction(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public void createAttraction(String id, String name, int capacity) throws ParkException {
        requireText(id, "attraction identifier");
        requireText(name, "attraction name");
        if (capacity <= 0) {
            throw new ParkException("capacity must be a positive integer");
        }
        if (findAttraction(id).isPresent()) {
            throw new ParkException("attraction " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        persist(() -> attractions.remove(attraction));
    }

    public Attraction requireAttraction(String id) throws ParkException {
        return findAttraction(id).orElseThrow(() -> new ParkException("attraction " + id + " not found"));
    }

    /**
     * Admits every holder of the ticket together, or nobody. The same mechanism serves
     * individual and group tickets. Occupancy is transient and is not persisted.
     */
    public List<User> enter(String attractionId, String ticketId) throws ParkException {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId).orElseThrow(() -> new ParkException("ticket " + ticketId + " not found"));
        if (!ticket.isValidOn(currentDate)) {
            throw new ParkException("ticket " + ticketId + " is not valid on " + currentDate);
        }
        List<User> holders = ticket.getHolders();
        if (holders.stream().anyMatch(attraction::isInside)) {
            throw new ParkException("a ticket holder is already inside " + attractionId);
        }
        if (holders.size() > attraction.getFreePlaces()) {
            throw new ParkException("not enough places in " + attractionId + " (" + attraction.getFreePlaces()
                    + " free, " + holders.size() + " needed)");
        }
        attraction.admit(holders);
        return holders;
    }

    public void exit(String attractionId, String email) throws ParkException {
        Attraction attraction = requireAttraction(attractionId);
        User occupant = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new ParkException("user " + email + " is not inside " + attractionId));
        attraction.release(occupant);
    }

    // ---- helpers ---------------------------------------------------------------------

    private Stream<IndividualTicket> allIndividualTickets() {
        return tickets.stream().flatMap(t -> t.getIndividualTickets().stream());
    }

    private void removeFutureTicket(Ticket ticket) throws ParkException {
        if (!ticket.isDeletableOn(currentDate)) {
            throw new ParkException("only tickets dated after " + currentDate + " can be deleted");
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        persist(() -> tickets.add(index, ticket));
    }

    private void requireUnusedIds(List<String> ids) throws ParkException {
        Set<String> seen = new HashSet<>();
        for (String id : ids) {
            if (!seen.add(id) || findTicket(id).isPresent()) {
                throw new ParkException("ticket identifier " + id + " already in use");
            }
        }
    }

    private void requireNotPast(LocalDate date) throws ParkException {
        if (date.isBefore(currentDate)) {
            throw new ParkException("date " + date + " is before the current date " + currentDate);
        }
    }

    private User requireUser(String email) throws ParkException {
        return users.findByEmail(email).orElseThrow(() -> new ParkException("user " + email + " not found"));
    }

    private Group requireGroup(String id) throws ParkException {
        return findGroup(id).orElseThrow(() -> new ParkException("group " + id + " not found"));
    }

    private Group requireModifiableGroup(String id) throws ParkException {
        Group group = requireGroup(id);
        if (isLocked(group)) {
            throw new ParkException("group " + id + " has an active group ticket");
        }
        return group;
    }

    private static void requireText(String value, String what) throws ParkException {
        if (value == null || value.isBlank()) {
            throw new ParkException(what + " must not be empty");
        }
    }

    private void persist(Runnable undo) throws ParkException {
        try {
            storage.save(users.getUsers(), groups, tickets, attractions);
        } catch (IOException e) {
            undo.run();
            throw new ParkException("could not save park data: " + e.getMessage());
        }
    }
}
