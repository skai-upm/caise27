package es.upm.park.service;

import es.upm.park.model.Attraction;
import es.upm.park.model.Group;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.persistence.AttractionFileRepository;
import es.upm.park.persistence.DataPaths;
import es.upm.park.persistence.GroupFileRepository;
import es.upm.park.persistence.TicketFileRepository;
import es.upm.park.persistence.UserFileRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management. Exactly one instance exists per execution; it
 * holds the current system date and base price and every user, group,
 * ticket and attraction, and answers questions that cross those entities.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private UserService users;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private GroupFileRepository groupRepository;
    private TicketFileRepository ticketRepository;
    private AttractionFileRepository attractionRepository;

    private Park() {
    }

    private static final class Holder {
        private static final Park INSTANCE = new Park();
    }

    public static Park getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Loads the park state stored at the given paths and resets the system
     * date to today and the base price to {@link #INITIAL_PRICE}.
     */
    public void open(DataPaths paths) {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
        groups.clear();
        tickets.clear();
        attractions.clear();

        users = new UserService(new UserFileRepository(paths.users()));
        groupRepository = new GroupFileRepository(paths.groups());
        ticketRepository = new TicketFileRepository(paths.tickets());
        attractionRepository = new AttractionFileRepository(paths.attractions());

        for (Group group : groupRepository.loadAll(users::findByDni)) {
            groups.put(group.getId(), group);
        }
        for (Ticket ticket : ticketRepository.loadAll(users::findByDni, groups::get)) {
            tickets.put(ticket.getId(), ticket);
        }
        for (Attraction attraction : attractionRepository.loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
    }

    /**
     * Writes the whole park state to its data files.
     */
    public void save() {
        users.persist();
        groupRepository.saveAll(groups.values());
        ticketRepository.saveAll(tickets.values());
        attractionRepository.saveAll(attractions.values());
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public UserService users() {
        return users;
    }

    public Group findGroup(String id) {
        return groups.get(id);
    }

    Collection<Group> groups() {
        return groups.values();
    }

    void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    void removeGroup(Group group) {
        groups.remove(group.getId());
    }

    public Attraction findAttraction(String id) {
        return attractions.get(id);
    }

    Collection<Attraction> attractions() {
        return attractions.values();
    }

    void addAttraction(Attraction attraction) {
        attractions.put(attraction.getId(), attraction);
    }

    /**
     * Top-level tickets: standalone individual tickets and group tickets.
     */
    public Collection<Ticket> tickets() {
        return tickets.values();
    }

    void addTicket(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    void removeTicket(Ticket ticket) {
        tickets.remove(ticket.getId());
    }

    /**
     * Finds any ticket in the shared identifier space, including the
     * individual tickets that compose group tickets.
     */
    public Ticket findTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket) {
                for (IndividualTicket component : groupTicket.getComposition()) {
                    if (component.getId().equals(id)) {
                        return component;
                    }
                }
            }
        }
        return null;
    }

    public boolean isTicketIdTaken(String id) {
        return findTicket(id) != null;
    }

    public GroupTicket findContainingGroupTicket(IndividualTicket ticket) {
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket && groupTicket.getComposition().contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    /**
     * A group is locked while any group ticket it bought is still active.
     */
    public boolean isLocked(Group group) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && groupTicket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the user holds an active ticket, standalone or as part of a
     * group ticket.
     */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.isActiveOn(currentDate) && ticket.involves(user)) {
                return true;
            }
        }
        return false;
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    /**
     * Deletes a user after checking credentials, active tickets and locked
     * groups; the user is removed from every group it belonged to.
     */
    public void deleteUser(String email, String password) throws OperationException {
        User user = users.authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new OperationException("The user " + email + " has active tickets and cannot be deleted");
        }
        List<Group> memberships = groupsOf(user);
        for (Group group : memberships) {
            if (isLocked(group)) {
                throw new OperationException("The user " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        users.remove(user);
        save();
    }
}
