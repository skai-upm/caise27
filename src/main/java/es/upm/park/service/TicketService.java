package es.upm.park.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import es.upm.park.model.Group;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.repository.TicketRepository;
import es.upm.park.repository.UserRepository;

/**
 * Ticket operations. Tickets associated with a user are found by traversing
 * the stored tickets; users keep no references to tickets.
 */
public class TicketService {

    private final TicketRepository tickets;
    private final UserRepository users;
    private final Supplier<LocalDate> today;
    private final Supplier<BigDecimal> basePrice;

    public TicketService(TicketRepository tickets, UserRepository users,
            Supplier<LocalDate> today, Supplier<BigDecimal> basePrice) {
        this.tickets = tickets;
        this.users = users;
        this.today = today;
        this.basePrice = basePrice;
    }

    /**
     * Creates an individual ticket at the current base price.
     */
    public IndividualTicket createIndividual(String id, String email, LocalDate date) {
        checkNewId(id);
        User user = users.findByEmail(email).orElseThrow(() -> new UserException("no user with email " + email));
        checkNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, basePrice.get(), user);
        tickets.add(ticket);
        return ticket;
    }

    /**
     * Deletes an individual ticket dated strictly after the current date.
     */
    public void deleteIndividual(String id) {
        groupTickets().filter(g -> g.findConstituent(id).isPresent()).findFirst().ifPresent(g -> {
            throw new TicketException("ticket " + id + " belongs to group ticket " + g.getId()
                    + " and cannot be deleted independently");
        });
        Ticket ticket = tickets.findById(id)
                .filter(IndividualTicket.class::isInstance)
                .orElseThrow(() -> new TicketException("no ticket " + id));
        checkDeletable(ticket);
        tickets.remove(ticket);
    }

    /**
     * Creates a group ticket for the current members of a non-empty group.
     * All identifiers are checked first so nothing is created on conflict.
     */
    public GroupTicket createGroupTicket(String id, Group group, LocalDate date) {
        checkNewId(id);
        if (group.getMemberDnis().isEmpty()) {
            throw new TicketException("group " + group.getId() + " is empty");
        }
        checkNotPast(date);
        List<User> members = new ArrayList<>();
        for (String dni : group.getMemberDnis()) {
            String constituent = GroupTicket.constituentId(id, dni);
            if (tickets.existsId(constituent)) {
                throw new TicketException("ticket identifier " + constituent + " already exists");
            }
            members.add(users.findByDni(dni).orElseThrow(() -> new UserException("no user with DNI " + dni)));
        }
        GroupTicket ticket = new GroupTicket(id, date, basePrice.get(), group, members);
        tickets.add(ticket);
        return ticket;
    }

    /**
     * Deletes a group ticket dated strictly after the current date, together
     * with its composition.
     */
    public void deleteGroupTicket(String id) {
        GroupTicket ticket = groupTickets().filter(g -> g.getId().equals(id)).findFirst()
                .orElseThrow(() -> new TicketException("no group ticket " + id));
        checkDeletable(ticket);
        tickets.remove(ticket);
    }

    /** Whether the group has a group ticket dated on or after the current date. */
    public boolean hasActiveGroupTicket(Group group) {
        LocalDate now = today.get();
        return groupTickets().anyMatch(g -> g.getGroup() == group && g.isActive(now));
    }

    /** Clears the group reference of the tickets of a deleted group. */
    public void detachGroup(Group group) {
        groupTickets().filter(g -> g.getGroup() == group).forEach(GroupTicket::detachGroup);
        tickets.save();
    }

    private java.util.stream.Stream<GroupTicket> groupTickets() {
        return tickets.findAll().stream().filter(GroupTicket.class::isInstance).map(GroupTicket.class::cast);
    }

    public Ticket find(String id) {
        return tickets.findById(id).orElseThrow(() -> new TicketException("no ticket " + id));
    }

    /** A top-level ticket of any type usable for attraction access. */
    public Ticket findForAccess(String id) {
        return find(id);
    }

    /** Number of top-level tickets (individual and group tickets). */
    public int count() {
        return tickets.findAll().size();
    }

    /** Whether any ticket dated on or after the current date concerns the user. */
    public boolean hasActiveTicket(User user) {
        LocalDate now = today.get();
        return tickets.findAll().stream().anyMatch(t -> t.involves(user) && t.isActive(now));
    }

    void checkNewId(String id) {
        if (id == null || id.isBlank()) {
            throw new TicketException("ticket identifier is required");
        }
        if (tickets.existsId(id)) {
            throw new TicketException("ticket " + id + " already exists");
        }
    }

    void checkNotPast(LocalDate date) {
        if (date.isBefore(today.get())) {
            throw new TicketException("ticket date " + date + " is before the current date " + today.get());
        }
    }

    void checkDeletable(Ticket ticket) {
        if (!ticket.isDeletable(today.get())) {
            throw new TicketException("ticket " + ticket.getId() + " cannot be deleted: it is not dated after "
                    + today.get());
        }
    }
}
