package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.validation.UserValidator;
import es.upm.etsisi.park.persistence.TicketRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Singleton managing the ticket registry and the shared ticket identifier space.
 */
public final class TicketController {

    private static TicketController instance;

    private TicketRepository repository;
    private SystemController system;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    private TicketController() {
    }

    public static synchronized TicketController getInstance() {
        if (instance == null) {
            instance = new TicketController();
        }
        return instance;
    }

    public void initialize(TicketRepository ticketRepository, SystemController systemController,
                           UserController users, Function<String, Optional<Group>> groupById) {
        this.repository = ticketRepository;
        this.system = systemController;
        tickets.clear();
        for (Ticket ticket : ticketRepository.loadAll(users::findByDni, groupById)) {
            tickets.put(ticket.getId(), ticket);
        }
    }

    public IndividualTicket createIndividual(String id, User user, String dateText) {
        requireIdentifier(id);
        IndividualTicket ticket = new IndividualTicket(id, requireUsableDate(dateText), system.getCurrentPrice(), user);
        register(ticket);
        return ticket;
    }

    public GroupTicket createGroupTicket(String id, Group group, String dateText) {
        requireIdentifier(id);
        LocalDate date = requireUsableDate(dateText);
        if (group.isEmpty()) {
            throw new OperationNotAllowedException("Group " + group.getId() + " has no members");
        }
        GroupTicket ticket = new GroupTicket(id, date, system.getCurrentPrice(), group, group.getMembers());
        register(ticket);
        return ticket;
    }

    /**
     * Checks that {@code TICKET DELETE} may remove this ticket, without changing anything.
     */
    public IndividualTicket checkIndividualDeletion(String id) {
        Ticket ticket = require(id);
        if (ticket instanceof GroupTicket) {
            throw new OperationNotAllowedException("Ticket " + id + " is a group ticket. Use GROUPTICKET DELETE");
        }
        if (!tickets.containsKey(id)) {
            throw new OperationNotAllowedException("Ticket " + id
                    + " belongs to a group ticket and cannot be deleted on its own");
        }
        requireDeletable(ticket);
        return (IndividualTicket) ticket;
    }

    /**
     * Checks that {@code GROUPTICKET DELETE} may remove this group ticket, without changing anything.
     */
    public GroupTicket checkGroupDeletion(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new NotFoundException("Group ticket " + id + " does not exist");
        }
        requireDeletable(ticket);
        return groupTicket;
    }

    public void remove(Ticket ticket) {
        tickets.remove(ticket.getId());
        persist();
    }

    public void detachGroup(Group group) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTicket.detachGroup();
            }
        }
        persist();
    }

    public Optional<Ticket> find(String id) {
        for (Ticket ticket : tickets.values()) {
            Optional<Ticket> found = ticket.findTicket(id);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    public Ticket require(String id) {
        return find(id).orElseThrow(() -> new NotFoundException("Ticket " + id + " does not exist"));
    }

    public Optional<IndividualTicket> findIndividual(String id) {
        return find(id).filter(IndividualTicket.class::isInstance).map(IndividualTicket.class::cast);
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    public boolean hasActiveTicket(User user) {
        LocalDate today = system.getCurrentDate();
        for (Ticket ticket : tickets.values()) {
            if (ticket.isActiveOn(today) && ticket.involves(user)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGroupLocked(Group group) {
        LocalDate today = system.getCurrentDate();
        for (GroupTicket ticket : groupTicketsOf(group)) {
            if (ticket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Individual tickets bought directly for the user (not as part of a group ticket).
     */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser().equals(user)) {
                result.add(individual);
            }
        }
        return result;
    }

    /**
     * Every individual ticket held by the user, including those inside group tickets.
     */
    public List<IndividualTicket> ticketsHeldBy(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            for (IndividualTicket individual : ticket.getAccessTickets()) {
                if (individual.getUser().equals(user)) {
                    result.add(individual);
                }
            }
        }
        return result;
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    public void persist() {
        repository.saveAll(tickets.values());
    }

    private void register(Ticket ticket) {
        Set<String> used = new HashSet<>();
        for (Ticket existing : tickets.values()) {
            used.addAll(existing.getIdentifiers());
        }
        for (String identifier : ticket.getIdentifiers()) {
            if (used.contains(identifier)) {
                throw new DuplicateException("Ticket identifier " + identifier + " is already in use");
            }
        }
        tickets.put(ticket.getId(), ticket);
        persist();
    }

    private LocalDate requireUsableDate(String text) {
        LocalDate date = SystemController.parseDate(text);
        if (date.isBefore(system.getCurrentDate())) {
            throw new OperationNotAllowedException("Date " + date + " is before the current system date "
                    + system.getCurrentDate());
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) {
        if (!ticket.isDeletableOn(system.getCurrentDate())) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId()
                    + " is for the current day or earlier and must be kept as a historical record");
        }
    }

    private static void requireIdentifier(String id) {
        if (!UserValidator.isNotBlank(id)) {
            throw new ValidationException("Ticket identifier must not be empty");
        }
    }
}
