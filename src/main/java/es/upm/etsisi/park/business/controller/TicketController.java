package es.upm.etsisi.park.business.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.ticket.GroupDiscount;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Ticket;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.persistence.TicketRepository;

/**
 * Manages individual and group tickets and their shared identifier space.
 * Deleting a ticket also deletes the passes bought for it. Singleton.
 */
public final class TicketController {

    private static final TicketController INSTANCE = new TicketController();

    private TicketRepository repository;
    private SystemController system;
    private UserController users;
    private GroupController groups;
    private PassController passes;
    /** Top-level tickets: standalone individual tickets and group tickets. */
    private final List<Ticket> tickets = new ArrayList<>();
    /** Every ticket by upper-cased identifier, including member tickets. */
    private final Map<String, Ticket> index = new HashMap<>();

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(TicketRepository repository, SystemController system, UserController users,
                          GroupController groups, PassController passes) {
        this.repository = repository;
        this.system = system;
        this.users = users;
        this.groups = groups;
        this.passes = passes;
        tickets.clear();
        index.clear();
    }

    public void load() throws PersistenceException {
        tickets.clear();
        index.clear();
        try {
            for (Ticket ticket : repository.load(users.usersByDni(), groups.groupsById())) {
                register(ticket);
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not load tickets: " + e.getMessage(), e);
        }
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    /** Any ticket by identifier, including member tickets of group tickets. */
    public Ticket find(String id) {
        return index.get(Checks.key(id));
    }

    public Ticket require(String id) throws NotFoundException {
        Ticket ticket = find(id);
        if (ticket == null) {
            throw new NotFoundException("Ticket " + id + " not found");
        }
        return ticket;
    }

    /** An individual ticket (standalone or member of a group ticket). */
    public IndividualTicket requireIndividual(String id) throws ParkException {
        Ticket ticket = require(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ValidationException("Ticket " + ticket.getId() + " is not an individual ticket");
        }
        return individual;
    }

    /** Single check of the identifier space shared by every ticket type. */
    public boolean isIdTaken(String id) {
        return index.containsKey(Checks.key(id));
    }

    /** Every individual ticket, standalone or member, by its exact identifier. */
    public Map<String, IndividualTicket> individualTicketsById() {
        Map<String, IndividualTicket> result = new HashMap<>();
        for (Ticket ticket : index.values()) {
            if (ticket instanceof IndividualTicket individual) {
                result.put(individual.getId(), individual);
            }
        }
        return result;
    }

    public IndividualTicket createTicket(String id, String email, String dateText) throws ParkException {
        requireFreeId(id);
        User user = users.require(email);
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, system.getBasePrice());
        register(ticket);
        persist();
        return ticket;
    }

    /** Deletes a future standalone individual ticket together with its passes. */
    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = require(id);
        if (ticket instanceof GroupTicket) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " is a group ticket: use GROUPTICKET DELETE");
        }
        GroupTicket container = containerOf(ticket);
        if (container != null) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " belongs to group ticket "
                    + container.getId() + " and cannot be deleted on its own");
        }
        requireDeletable(ticket);
        removeWithPasses(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        requireFreeId(id);
        Group group = groups.require(groupId);
        if (group.isEmpty()) {
            throw new OperationNotAllowedException("Group " + group.getId() + " has no members");
        }
        LocalDate date = requireUsableDate(dateText);
        List<User> members = group.getMembers();
        GroupDiscount discount = GroupDiscount.forMembers(members.size());
        List<IndividualTicket> memberTickets = new ArrayList<>();
        for (User member : members) {
            String memberId = GroupTicket.memberId(id, member.getDni());
            requireFreeId(memberId);
            memberTickets.add(new IndividualTicket(memberId, date, member, system.getBasePrice(),
                    discount.getPercent()));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, memberTickets);
        register(ticket);
        persist();
        return ticket;
    }

    /** Deletes a future group ticket with its member tickets and all their passes. */
    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = require(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new ValidationException("Ticket " + ticket.getId() + " is not a group ticket");
        }
        requireDeletable(ticket);
        removeWithPasses(ticket);
    }

    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets) {
            if (ticket.isActiveOn(system.getDate()) && ticket.admits(user)) {
                return true;
            }
        }
        return false;
    }

    /** Group tickets bought by the group, found by traversing the tickets. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    /** Standalone individual tickets bought for the user (not member tickets). */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            }
        }
        return result;
    }

    /** Every individual ticket of the user: standalone ones and member tickets of group tickets. */
    public List<IndividualTicket> individualTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            for (IndividualTicket individual : ticket.getIndividualTickets()) {
                if (individual.getUser() == user) {
                    result.add(individual);
                }
            }
        }
        return result;
    }

    /** The group ticket whose composition contains {@code ticket}, or null. */
    public GroupTicket containerOf(Ticket ticket) {
        for (Ticket candidate : tickets) {
            if (candidate instanceof GroupTicket groupTicket && groupTicket.getMembers().contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    /** Called when a group is deleted: historical group tickets lose their group reference. */
    void detachGroup(Group group) throws PersistenceException {
        for (GroupTicket ticket : groupTicketsOf(group)) {
            ticket.detachGroup();
        }
        persist();
    }

    /** Saves the tickets; users no longer active are stored as former users. */
    public void persist() throws PersistenceException {
        try {
            repository.save(tickets, users.getUsers());
        } catch (IOException e) {
            throw new PersistenceException("Could not save tickets: " + e.getMessage(), e);
        }
    }

    private void removeWithPasses(Ticket ticket) throws PersistenceException {
        List<Pass> related = new ArrayList<>();
        for (IndividualTicket individual : ticket.getIndividualTickets()) {
            related.addAll(individual.getPasses());
        }
        passes.discard(related);
        unregister(ticket);
        passes.persist();
        persist();
    }

    private void register(Ticket ticket) {
        tickets.add(ticket);
        index.put(Checks.key(ticket.getId()), ticket);
        if (ticket instanceof GroupTicket groupTicket) {
            for (IndividualTicket member : groupTicket.getMembers()) {
                index.put(Checks.key(member.getId()), member);
            }
        }
    }

    private void unregister(Ticket ticket) {
        tickets.remove(ticket);
        index.remove(Checks.key(ticket.getId()));
        if (ticket instanceof GroupTicket groupTicket) {
            for (IndividualTicket member : groupTicket.getMembers()) {
                index.remove(Checks.key(member.getId()));
            }
        }
    }

    private void requireFreeId(String id) throws ParkException {
        Checks.requireText(id, "Ticket identifier");
        if (isIdTaken(id)) {
            throw new DuplicateException("Ticket identifier " + id + " is already in use");
        }
    }

    LocalDate requireUsableDate(String text) throws ParkException {
        LocalDate date = SystemController.parseDate(text);
        if (date.isBefore(system.getDate())) {
            throw new OperationNotAllowedException("Date " + date + " is before the current date " + system.getDate());
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) throws OperationNotAllowedException {
        if (!ticket.isDeletableOn(system.getDate())) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + "; only tickets after " + system.getDate() + " can be deleted");
        }
    }
}
