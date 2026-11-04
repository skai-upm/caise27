package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for individual and group tickets. Dates are always checked
 * against the system date passed in by the controller. Every successful change
 * is saved; if saving fails the previous state is restored.
 */
public class TicketManager {

    private final TicketFileRepository repository;
    private final GroupTicketFileRepository groupRepository;
    private final UserManager users;
    private final Map<String, IndividualTicket> tickets = new LinkedHashMap<>();
    private final Map<String, GroupTicket> groupTickets = new LinkedHashMap<>();

    public TicketManager(TicketFileRepository repository, GroupTicketFileRepository groupRepository,
                         UserManager users, GroupManager groups) throws IOException {
        this.repository = repository;
        this.groupRepository = groupRepository;
        this.users = users;
        Map<String, User> byDni = new HashMap<>();
        for (User u : users.getUsers()) {
            byDni.put(u.getDni(), u);
        }
        for (IndividualTicket t : repository.load(byDni::get)) {
            tickets.put(t.getId(), t);
        }
        for (GroupTicket t : groupRepository.load(id -> groups.find(id).orElse(null), byDni::get)) {
            groupTickets.put(t.getId(), t);
        }
    }

    /** Individual tickets bought with {@code TICKET CREATE}. */
    public Collection<IndividualTicket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    public Collection<GroupTicket> getGroupTickets() {
        return Collections.unmodifiableCollection(groupTickets.values());
    }

    /** Every ticket, individual and group, through the common abstraction. */
    public List<Ticket> allTickets() {
        List<Ticket> all = new ArrayList<>(tickets.values());
        all.addAll(groupTickets.values());
        return all;
    }

    public Optional<IndividualTicket> find(String id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public Optional<GroupTicket> findGroupTicket(String id) {
        return Optional.ofNullable(groupTickets.get(id));
    }

    /** Creates a ticket for the user with that email on a date not before {@code today}. */
    public IndividualTicket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price)
            throws UserException {
        requireNewId(id);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UserException("No user with email " + email));
        requireNotPast(date, today);
        IndividualTicket ticket = new IndividualTicket(id, date, user, price);
        tickets.put(id, ticket);
        saveTickets(() -> tickets.remove(id));
        return ticket;
    }

    /** Buys a group ticket for the current members of {@code group}. */
    public GroupTicket createGroupTicket(String id, Group group, LocalDate date, LocalDate today, BigDecimal price)
            throws UserException {
        requireNewId(id);
        if (group.getMembers().isEmpty()) {
            throw new UserException("Group " + group.getId() + " has no members");
        }
        requireNotPast(date, today);
        GroupTicket ticket = new GroupTicket(id, date, group, price);
        for (IndividualTicket part : ticket.getParts()) {
            if (idInUse(part.getId())) {
                throw new UserException("A ticket with id " + part.getId() + " already exists");
            }
        }
        groupTickets.put(id, ticket);
        saveGroupTickets(() -> groupTickets.remove(id));
        return ticket;
    }

    /** Deletes an individual ticket whose date is later than {@code today}. */
    public void delete(String id, LocalDate today) throws UserException {
        IndividualTicket ticket = tickets.get(id);
        if (ticket == null) {
            if (groupTickets.containsKey(id)) {
                throw new UserException(id + " is a group ticket; use GROUPTICKET DELETE");
            }
            Optional<GroupTicket> owner = findOwningGroupTicket(id);
            if (owner.isPresent()) {
                throw new UserException("Ticket " + id + " is part of group ticket " + owner.get().getId()
                        + "; use GROUPTICKET DELETE");
            }
            throw new UserException("No ticket with id " + id);
        }
        requireFuture(ticket, today);
        Map<String, IndividualTicket> previous = new LinkedHashMap<>(tickets);
        tickets.remove(id);
        saveTickets(() -> {
            tickets.clear();
            tickets.putAll(previous);
        });
    }

    /** Deletes a group ticket, with all its individual tickets, if its date is later than {@code today}. */
    public void deleteGroupTicket(String id, LocalDate today) throws UserException {
        GroupTicket ticket = groupTickets.get(id);
        if (ticket == null) {
            throw new UserException(tickets.containsKey(id)
                    ? id + " is an individual ticket; use TICKET DELETE"
                    : "No group ticket with id " + id);
        }
        requireFuture(ticket, today);
        Map<String, GroupTicket> previous = new LinkedHashMap<>(groupTickets);
        groupTickets.remove(id);
        saveGroupTickets(() -> {
            groupTickets.clear();
            groupTickets.putAll(previous);
        });
    }

    /** True if the user owns an individual or group-generated ticket dated on or after {@code today}. */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        return individualTicketsIncludingParts().stream()
                .anyMatch(t -> t.belongsTo(user) && t.isOnOrAfter(today));
    }

    /** True if the group bought a group ticket dated on or after {@code today}. */
    public boolean groupHasTicketOnOrAfter(Group group, LocalDate today) {
        return groupTickets.values().stream().anyMatch(t -> t.belongsTo(group) && t.isOnOrAfter(today));
    }

    /** Keeps the user's tickets as history without a reference to the deleted user. */
    public void detachUser(User user) {
        individualTicketsIncludingParts().stream().filter(t -> t.belongsTo(user))
                .forEach(IndividualTicket::detachUser);
    }

    /** Keeps the group's tickets as history without a reference to the deleted group. */
    public void detachGroup(Group group) {
        groupTickets.values().stream().filter(t -> t.belongsTo(group)).forEach(GroupTicket::detachGroup);
    }

    private List<IndividualTicket> individualTicketsIncludingParts() {
        List<IndividualTicket> all = new ArrayList<>(tickets.values());
        for (GroupTicket g : groupTickets.values()) {
            all.addAll(g.getParts());
        }
        return all;
    }

    private Optional<GroupTicket> findOwningGroupTicket(String partId) {
        return groupTickets.values().stream()
                .filter(g -> g.getParts().stream().anyMatch(p -> p.getId().equals(partId)))
                .findFirst();
    }

    private boolean idInUse(String id) {
        return tickets.containsKey(id) || groupTickets.containsKey(id) || findOwningGroupTicket(id).isPresent();
    }

    private void requireNewId(String id) throws UserException {
        if (!UserValidator.isNotBlank(id)) {
            throw new UserException("The ticket id must not be empty");
        }
        if (idInUse(id)) {
            throw new UserException("A ticket with id " + id + " already exists");
        }
    }

    private static void requireNotPast(LocalDate date, LocalDate today) throws UserException {
        if (date.isBefore(today)) {
            throw new UserException("Cannot buy a ticket for a past date (" + date + " is before " + today + ")");
        }
    }

    private static void requireFuture(Ticket ticket, LocalDate today) throws UserException {
        if (!ticket.getDate().isAfter(today)) {
            throw new UserException("Ticket " + ticket.getId() + " is for " + ticket.getDate()
                    + " and can only be deleted before that date");
        }
    }

    private void saveTickets(Runnable rollback) throws UserException {
        try {
            repository.save(tickets.values());
        } catch (IOException e) {
            rollback.run();
            throw new UserException("Could not save tickets: " + e.getMessage());
        }
    }

    private void saveGroupTickets(Runnable rollback) throws UserException {
        try {
            groupRepository.save(groupTickets.values());
        } catch (IOException e) {
            rollback.run();
            throw new UserException("Could not save group tickets: " + e.getMessage());
        }
    }
}
