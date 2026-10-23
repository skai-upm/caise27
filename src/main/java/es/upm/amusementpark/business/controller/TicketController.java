package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.TransactionManager;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages individual and group tickets, which share one identifier space.
 */
public final class TicketController extends AbstractController {

    private static final TicketController INSTANCE = new TicketController();

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private TicketRepository repository;
    private SystemController system;
    private UserController users;
    private GroupController groups;
    private PassController passes;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and the collaborating controllers. */
    public void configure(TicketRepository repository, TransactionManager transactions, SystemController system,
                          UserController users, GroupController groups, PassController passes) {
        this.repository = repository;
        setTransactions(transactions);
        this.system = system;
        this.users = users;
        this.groups = groups;
        this.passes = passes;
        tickets.clear();
    }

    void load() throws IOException {
        tickets.clear();
        for (Ticket ticket : repository.loadAll(users::findByDni, groups::find)) {
            tickets.put(ticket.getId(), ticket);
        }
    }

    void save() throws IOException {
        repository.saveAll(tickets.values());
    }

    LocalDate today() {
        return system.getDate();
    }

    /**
     * Finds a ticket of any kind by identifier, including the individual
     * tickets that compose group tickets.
     */
    public Ticket find(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket) {
                for (IndividualTicket member : groupTicket.getComposition()) {
                    if (member.getId().equals(id)) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    public Ticket require(String id) throws NotFoundException {
        Ticket ticket = find(id);
        if (ticket == null) {
            throw new NotFoundException("No ticket with identifier " + id);
        }
        return ticket;
    }

    /** Standalone tickets and group tickets, without the individual tickets inside group tickets. */
    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    /** Single uniqueness check shared by every ticket type. */
    public boolean isIdTaken(String id) {
        return find(id) != null;
    }

    public IndividualTicket create(String id, String email, String dateText) throws ParkException {
        requireText(id, "Ticket identifier");
        requireFreeId(id);
        User user = users.require(email);
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, system.getPrice());
        tickets.put(id, ticket);
        persist(this::save, () -> tickets.remove(id));
        return ticket;
    }

    /** Deletes a standalone individual ticket dated after the current date, with all its passes. */
    public void delete(String id) throws ParkException {
        Ticket ticket = require(id);
        if (ticket instanceof GroupTicket) {
            throw new OperationNotAllowedException("Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        GroupTicket container = containerOf(ticket);
        if (container != null) {
            throw new OperationNotAllowedException("Ticket " + id + " belongs to group ticket " + container.getId()
                    + " and can only be deleted with it");
        }
        requireDeletable(ticket);
        removeWithPasses(id, ticket, List.of((IndividualTicket) ticket));
    }

    /**
     * Buys a group ticket for the current members of a non-empty group. The
     * identifiers of every individual ticket are checked before anything is
     * created.
     */
    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        requireText(id, "Ticket identifier");
        requireFreeId(id);
        Group group = groups.require(groupId);
        if (group.isEmpty()) {
            throw new OperationNotAllowedException("Group " + groupId + " has no members");
        }
        LocalDate date = requireUsableDate(dateText);
        GroupTicket ticket = GroupTicket.purchase(id, date, group, system.getPrice());
        for (IndividualTicket member : ticket.getComposition()) {
            requireFreeId(member.getId());
        }
        tickets.put(id, ticket);
        persist(this::save, () -> tickets.remove(id));
        return ticket;
    }

    /** Deletes a group ticket dated after the current date, with its individual tickets and their passes. */
    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = require(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationNotAllowedException("Ticket " + id + " is not a group ticket");
        }
        requireDeletable(ticket);
        removeWithPasses(id, ticket, ((GroupTicket) ticket).getComposition());
    }

    /**
     * Removes a ticket together with every pass of {@code owners} (the ticket
     * itself or the composition of a group ticket), saving both in one
     * transaction.
     */
    private void removeWithPasses(String id, Ticket ticket, List<IndividualTicket> owners) throws ParkException {
        List<Pass> refunded = passes.removeFor(owners);
        tickets.remove(id);
        persist(() -> {
            save();
            passes.save();
        }, () -> {
            tickets.put(id, ticket);
            passes.restore(refunded);
        });
    }

    /** Whether {@code user} holds any ticket dated today or later. */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.isActiveOn(today()) && ticket.isHeldBy(user)) {
                return true;
            }
        }
        return false;
    }

    /** Whether {@code group} bought a group ticket dated today or later. */
    public boolean hasActiveGroupTicket(Group group) {
        for (GroupTicket ticket : groupTicketsOf(group)) {
            if (ticket.isActiveOn(today())) {
                return true;
            }
        }
        return false;
    }

    /** Group tickets bought by {@code group}, found by traversing the tickets. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> purchases = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                purchases.add(groupTicket);
            }
        }
        return purchases;
    }

    private GroupTicket containerOf(Ticket member) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getComposition().contains(member)) {
                return groupTicket;
            }
        }
        return null;
    }

    private void requireFreeId(String id) throws OperationNotAllowedException {
        if (isIdTaken(id)) {
            throw new OperationNotAllowedException("A ticket with identifier " + id + " already exists");
        }
    }

    private LocalDate requireUsableDate(String dateText) throws ParkException {
        LocalDate date = SystemController.parseDate(dateText);
        if (date.isBefore(today())) {
            throw new OperationNotAllowedException("Tickets cannot be created for past dates (current date "
                    + today() + ")");
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) throws OperationNotAllowedException {
        if (!ticket.isDeletableOn(today())) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + " and is kept as a historical record; only tickets after " + today() + " can be deleted");
        }
    }
}
