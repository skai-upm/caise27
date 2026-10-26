package es.upm.parque.business.controller;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.TicketRepository;
import es.upm.parque.business.validation.UserValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for individual and group tickets. Date checks always use the shared {@link SystemDate}.
 */
public class TicketController {
    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final TicketController INSTANCE = new TicketController();

    private TicketRepository repository;
    private UserController userController;
    private GroupController groupController;
    private SystemDate systemDate;
    private final List<Ticket> tickets = new ArrayList<>();
    private BigDecimal price = DEFAULT_PRICE;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and collaborators, reloads the tickets, resets the price and registers the ticket rules
     * on the user and group controllers (which must already be configured).
     */
    public TicketController configure(TicketRepository repository, UserController userController,
            GroupController groupController, SystemDate systemDate) {
        this.repository = repository;
        this.userController = userController;
        this.groupController = groupController;
        this.systemDate = systemDate;
        tickets.clear();
        tickets.addAll(repository.load(userController::findByDni, groupController::findById));
        price = DEFAULT_PRICE;
        userController.addDeletionCheck(
                user -> hasCurrentOrFutureTickets(user) ? "the user has current or future tickets" : null);
        groupController.addModificationCheck(group -> hasCurrentOrFutureGroupTickets(group)
                ? "the group has current or future group tickets" : null);
        return this;
    }

    public IndividualTicket create(String id, String email, LocalDate date) {
        requireNewId(id);
        User user = userController.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        requireUsableDate(date);
        IndividualTicket ticket = new IndividualTicket(id, date, user, price);
        tickets.add(ticket);
        repository.save(tickets);
        return ticket;
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        requireNewId(id);
        Group group = groupController.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("group " + groupId + " does not exist");
        }
        if (group.getMembers().isEmpty()) {
            throw new IllegalArgumentException("group " + groupId + " has no members");
        }
        requireUsableDate(date);
        List<IndividualTicket> parts = new ArrayList<>();
        int n = 1;
        for (User member : group.getMembers()) {
            String partId = id + "-" + n++;
            requireNewId(partId);
            parts.add(new IndividualTicket(partId, date, member, price));
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, date, group, parts);
        tickets.addAll(parts);
        tickets.add(groupTicket);
        repository.save(tickets);
        return groupTicket;
    }

    public IndividualTicket delete(String id) {
        Ticket ticket = findById(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new IllegalArgumentException("individual ticket " + id + " does not exist");
        }
        GroupTicket owner = groupTicketContaining(individual);
        if (owner != null) {
            throw new IllegalArgumentException("ticket " + id + " belongs to group ticket " + owner.getId()
                    + "; delete the group ticket instead");
        }
        requireFuture(individual);
        if (individual.hasPasses()) {
            throw new IllegalArgumentException("ticket " + id + " has passes and cannot be deleted");
        }
        tickets.remove(individual);
        repository.save(tickets);
        return individual;
    }

    public GroupTicket deleteGroupTicket(String id) {
        Ticket ticket = findById(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new IllegalArgumentException("group ticket " + id + " does not exist");
        }
        requireFuture(groupTicket);
        for (IndividualTicket part : groupTicket.getTickets()) {
            if (part.hasPasses()) {
                throw new IllegalArgumentException("ticket " + part.getId() + " of group ticket " + id
                        + " has passes and the group ticket cannot be deleted");
            }
        }
        tickets.removeAll(groupTicket.getTickets());
        tickets.remove(groupTicket);
        repository.save(tickets);
        return groupTicket;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal newPrice) {
        if (newPrice.signum() <= 0 || newPrice.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("the price must be a positive amount with at most two decimals");
        }
        price = newPrice.setScale(2, RoundingMode.UNNECESSARY);
    }

    public boolean hasCurrentOrFutureTickets(User user) {
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it && it.getUser() == user && t.isCurrentOrFuture(systemDate.get())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCurrentOrFutureGroupTickets(Group group) {
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.getGroup() == group && t.isCurrentOrFuture(systemDate.get())) {
                return true;
            }
        }
        return false;
    }

    /** The user's individual tickets that were not generated inside a group ticket. */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it && it.getUser() == user && groupTicketContaining(it) == null) {
                result.add(it);
            }
        }
        return result;
    }

    /** Every individual ticket of the user, including those generated inside group tickets. */
    public List<IndividualTicket> individualTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it && it.getUser() == user) {
                result.add(it);
            }
        }
        return result;
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.getGroup() == group) {
                result.add(gt);
            }
        }
        return result;
    }

    public Ticket findById(String id) {
        for (Ticket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    private GroupTicket groupTicketContaining(IndividualTicket ticket) {
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.getTickets().contains(ticket)) {
                return gt;
            }
        }
        return null;
    }

    private void requireNewId(String id) {
        if (!UserValidator.isNotBlank(id)) {
            throw new IllegalArgumentException("the ticket identifier cannot be empty");
        }
        if (findById(id) != null) {
            throw new IllegalArgumentException("ticket " + id + " already exists");
        }
    }

    private void requireUsableDate(LocalDate date) {
        if (date.isBefore(systemDate.get())) {
            throw new IllegalArgumentException("the ticket date " + date + " is before the system date "
                    + systemDate.get());
        }
    }

    private void requireFuture(Ticket ticket) {
        if (!ticket.isFuture(systemDate.get())) {
            throw new IllegalArgumentException("ticket " + ticket.getId() + " for " + ticket.getDate()
                    + " can only be deleted before its date (system date " + systemDate.get() + ")");
        }
    }
}
