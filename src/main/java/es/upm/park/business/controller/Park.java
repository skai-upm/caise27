package es.upm.park.business.controller;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.attraction.Attraction;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.persistence.Repositories;

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
 * ticket, attraction and pass, and answers questions that cross those
 * entities. Its repositories are injected with {@link #configure}.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private Repositories repositories;
    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private final List<User> users = new ArrayList<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private final Map<String, Pass> passes = new LinkedHashMap<>();

    private Park() {
    }

    private static final class Holder {
        private static final Park INSTANCE = new Park();
    }

    public static Park getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Repositories repositories) {
        this.repositories = repositories;
    }

    /**
     * Loads the stored park state through the configured repositories and
     * resets the system date to today and the base price to
     * {@link #INITIAL_PRICE}.
     */
    public void load() {
        Repositories repos = requireRepositories();
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
        passes.clear();

        users.addAll(repos.users().loadAll());
        for (Group group : repos.groups().loadAll(this::findUserByDni)) {
            groups.put(group.getId(), group);
        }
        for (Ticket ticket : repos.tickets().loadAll(this::findUserByDni, groups::get)) {
            tickets.put(ticket.getId(), ticket);
        }
        for (Attraction<?> attraction : repos.attractions().loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
        for (Pass pass : repos.passes().loadAll(this::findIndividualTicket)) {
            passes.put(pass.getId(), pass);
        }
    }

    /**
     * Writes the whole park state through the configured repositories.
     */
    public void save() {
        Repositories repos = requireRepositories();
        repos.users().saveAll(users);
        repos.groups().saveAll(groups.values());
        repos.tickets().saveAll(tickets.values());
        repos.attractions().saveAll(attractions.values());
        repos.passes().saveAll(passes.values());
    }

    private Repositories requireRepositories() {
        if (repositories == null) {
            throw new IllegalStateException("Park repositories have not been configured");
        }
        return repositories;
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

    // ---- users

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public User findUserByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    void addUser(User user) {
        users.add(user);
    }

    void removeUser(User user) {
        users.remove(user);
    }

    // ---- groups

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

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    // ---- attractions

    public Attraction<?> findAttraction(String id) {
        return attractions.get(id);
    }

    Collection<Attraction<?>> attractions() {
        return attractions.values();
    }

    void addAttraction(Attraction<?> attraction) {
        attractions.put(attraction.getId(), attraction);
    }

    // ---- tickets

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

    public IndividualTicket findIndividualTicket(String id) {
        return findTicket(id) instanceof IndividualTicket individual ? individual : null;
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
     * All individual tickets of a user: standalone ones and those composing
     * group tickets.
     */
    public List<IndividualTicket> individualTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            for (IndividualTicket individual : ticket.getAdmittedTickets()) {
                if (individual.getUser() == user) {
                    result.add(individual);
                }
            }
        }
        return result;
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

    // ---- passes

    public Pass findPass(String id) {
        return passes.get(id);
    }

    Collection<Pass> passes() {
        return passes.values();
    }

    void addPass(Pass pass) {
        passes.put(pass.getId(), pass);
    }

    void removePass(Pass pass) {
        passes.remove(pass.getId());
    }
}
