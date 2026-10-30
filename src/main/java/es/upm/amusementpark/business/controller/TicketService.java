package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Formats;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.TicketRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Individual and group ticket operations with persistence. Ticket identifiers share one namespace that
 * includes the constituent tickets of group tickets.
 */
public class TicketService {

    private static final String SAVE_ERROR = "ERROR: could not save tickets";

    private final TicketRepository repository;
    private final List<IndividualTicket> tickets;
    private final List<GroupTicket> groupTickets;

    public TicketService(TicketRepository repository, List<User> users, List<Group> groups) throws IOException {
        this.repository = repository;
        this.tickets = new ArrayList<>(repository.loadTickets(users));
        this.groupTickets = new ArrayList<>(repository.loadGroupTickets(users, groups));
    }

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    /** Finds any ticket by id: standalone individual, group, or constituent of a group ticket. */
    public Ticket findById(String id) {
        for (IndividualTicket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        for (GroupTicket gt : groupTickets) {
            if (gt.getId().equals(id)) {
                return gt;
            }
            for (IndividualTicket t : gt.getTickets()) {
                if (t.getId().equals(id)) {
                    return t;
                }
            }
        }
        return null;
    }

    public String createIndividual(String id, User user, LocalDate date, BigDecimal price, LocalDate today) {
        String error = checkNewTicket(id, date, today);
        if (error != null) {
            return error;
        }
        IndividualTicket ticket = new IndividualTicket(id, date, user, price);
        tickets.add(ticket);
        if (!persist()) {
            tickets.remove(ticket);
            return SAVE_ERROR;
        }
        return "OK TICKET CREATED -> " + Formats.formatPrice(price);
    }

    public String createGroup(String id, Group group, LocalDate date, BigDecimal basePrice, LocalDate today) {
        String error = checkNewTicket(id, date, today);
        if (error != null) {
            return error;
        }
        if (group.getMembers().isEmpty()) {
            return "ERROR: group " + group.getId() + " has no members";
        }
        GroupTicket ticket = GroupTicket.purchase(id, date, group, basePrice);
        for (IndividualTicket t : ticket.getTickets()) {
            if (findById(t.getId()) != null) {
                return "ERROR: ticket identifier " + t.getId() + " is already in use";
            }
        }
        groupTickets.add(ticket);
        if (!persist()) {
            groupTickets.remove(ticket);
            return SAVE_ERROR;
        }
        return "OK GROUP TICKET CREATED -> " + Formats.formatPrice(ticket.getPrice());
    }

    public String deleteIndividual(String id, LocalDate today) {
        IndividualTicket ticket = null;
        for (IndividualTicket t : tickets) {
            if (t.getId().equals(id)) {
                ticket = t;
            }
        }
        if (ticket == null) {
            return findById(id) != null
                    ? "ERROR: ticket " + id + " belongs to a group ticket; use GROUPTICKET DELETE"
                    : "ERROR: ticket " + id + " does not exist";
        }
        if (!ticket.isRefundableOn(today)) {
            return "ERROR: ticket " + id + " can only be deleted before its date";
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        if (!persist()) {
            tickets.add(index, ticket);
            return SAVE_ERROR;
        }
        return "OK: ticket " + id + " deleted";
    }

    public String deleteGroup(String id, LocalDate today) {
        GroupTicket ticket = null;
        for (GroupTicket gt : groupTickets) {
            if (gt.getId().equals(id)) {
                ticket = gt;
            }
        }
        if (ticket == null) {
            return "ERROR: group ticket " + id + " does not exist";
        }
        if (!ticket.isRefundableOn(today)) {
            return "ERROR: group ticket " + id + " can only be deleted before its date";
        }
        int index = groupTickets.indexOf(ticket);
        groupTickets.remove(index);
        if (!persist()) {
            groupTickets.add(index, ticket);
            return SAVE_ERROR;
        }
        return "OK: group ticket " + id + " deleted with its " + ticket.getTickets().size() + " tickets";
    }

    /** True if the user holds any ticket (standalone or in a group ticket) dated on or after {@code today}. */
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (IndividualTicket t : tickets) {
            if (t.getUser() == user && t.isActiveOn(today)) {
                return true;
            }
        }
        for (GroupTicket gt : groupTickets) {
            if (gt.isActiveOn(today) && gt.getUsers().contains(user)) {
                return true;
            }
        }
        return false;
    }

    /** True if the group has a group ticket dated on or after {@code today}. */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (GroupTicket gt : groupTickets) {
            if (gt.getGroup() == group && gt.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    private String checkNewTicket(String id, LocalDate date, LocalDate today) {
        if (!UserValidator.isNotBlank(id)) {
            return "ERROR: ticket identifier must not be empty";
        }
        if (findById(id) != null) {
            return "ERROR: ticket identifier " + id + " is already in use";
        }
        if (date.isBefore(today)) {
            return "ERROR: tickets cannot be bought for a past date";
        }
        return null;
    }

    private boolean persist() {
        try {
            repository.save(tickets, groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
