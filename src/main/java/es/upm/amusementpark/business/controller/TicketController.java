package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;

/**
 * Creates and deletes individual and group tickets, answers date-based questions
 * about them, and keeps the repository in sync.
 */
public class TicketController {

    private final TicketRepository repository;
    private final List<Ticket> tickets;

    public TicketController(TicketRepository repository, Function<String, User> userByDni,
                         Function<String, Group> groupById) throws IOException {
        this.repository = repository;
        this.tickets = repository.load(userByDni, groupById);
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    /**
     * Finds a ticket by identifier, including tickets owned by a group ticket.
     */
    public Ticket find(String id) {
        for (Ticket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
            if (t instanceof GroupTicket gt) {
                for (IndividualTicket c : gt.getTickets()) {
                    if (c.getId().equals(id)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private GroupTicket ownerOf(IndividualTicket ticket) {
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.getTickets().contains(ticket)) {
                return gt;
            }
        }
        return null;
    }

    public IndividualTicket createIndividual(String id, User user, LocalDate date, BigDecimal price)
            throws OperationException {
        requireFreeId(id);
        IndividualTicket ticket = new IndividualTicket(id, date, price, user);
        tickets.add(ticket);
        save(() -> tickets.remove(ticket));
        return ticket;
    }

    public GroupTicket createGroup(String id, Group group, LocalDate date, BigDecimal basePrice)
            throws OperationException {
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationException("Group " + group.getId() + " has no members");
        }
        requireFreeId(id);
        BigDecimal factor = BigDecimal.ONE.subtract(discountFor(members.size()));
        BigDecimal share = basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(members.size())).multiply(factor)
                .setScale(2, RoundingMode.HALF_UP);
        List<IndividualTicket> constituents = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            String childId = id + "-" + (i + 1);
            requireFreeId(childId);
            constituents.add(new IndividualTicket(childId, date, share, members.get(i)));
        }
        GroupTicket ticket = new GroupTicket(id, date, total, group, constituents);
        tickets.add(ticket);
        save(() -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteIndividual(String id, LocalDate today) throws OperationException {
        Ticket ticket = find(id);
        if (ticket == null) {
            throw new OperationException("Unknown ticket: " + id);
        }
        if (ticket instanceof GroupTicket) {
            throw new OperationException("Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        GroupTicket owner = ownerOf((IndividualTicket) ticket);
        if (owner != null) {
            throw new OperationException("Ticket " + id + " belongs to group ticket " + owner.getId()
                    + "; use GROUPTICKET DELETE");
        }
        removeIfRefundable(ticket, today);
    }

    public void deleteGroup(String id, LocalDate today) throws OperationException {
        Ticket ticket = find(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationException("Unknown group ticket: " + id);
        }
        removeIfRefundable(ticket, today);
    }

    private void removeIfRefundable(Ticket ticket, LocalDate today) throws OperationException {
        if (!ticket.isRefundableOn(today)) {
            throw new OperationException("Ticket " + ticket.getId() + " dated " + ticket.getDate()
                    + " can no longer be deleted; only tickets after " + today + " can");
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        save(() -> tickets.add(index, ticket));
    }

    /**
     * True if the user holds an individual or group-owned ticket dated on or after today.
     */
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (Ticket t : tickets) {
            if (!t.isActiveOn(today)) {
                continue;
            }
            for (User u : t.getUsers()) {
                if (u.getDni().equals(user.getDni())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * True if the group has a group ticket dated on or after today.
     */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.getGroup().getId().equals(group.getId()) && gt.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rewrites the ticket file, e.g. to refresh holder details after a user is deleted.
     */
    public void save() throws OperationException {
        save(() -> { });
    }

    static BigDecimal discountFor(int members) {
        if (members < 3) {
            return BigDecimal.ZERO;
        }
        if (members <= 5) {
            return new BigDecimal("0.10");
        }
        if (members <= 10) {
            return new BigDecimal("0.20");
        }
        return new BigDecimal("0.50");
    }

    private void requireFreeId(String id) throws OperationException {
        if (id.isEmpty()) {
            throw new OperationException("Ticket identifier must not be empty");
        }
        if (find(id) != null) {
            throw new OperationException("A ticket with identifier " + id + " already exists");
        }
    }

    private void save(Runnable rollback) throws OperationException {
        try {
            repository.save(tickets);
        } catch (IOException e) {
            rollback.run();
            throw new OperationException("Could not save tickets: " + e.getMessage());
        }
    }
}
