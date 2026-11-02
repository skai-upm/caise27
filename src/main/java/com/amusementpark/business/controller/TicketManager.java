package com.amusementpark.business.controller;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Ticket;
import com.amusementpark.business.model.User;
import com.amusementpark.business.model.UserValidator;
import com.amusementpark.persistence.TicketFileStorage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Individual and group tickets: purchase, deletion, locks and persistence of both ticket files.
 * Every date rule receives the system date from the caller.
 */
public class TicketManager {

    private static final String SAVE_FAILED = "ERROR: Could not save tickets files.";

    private final TicketFileStorage storage;
    private final List<IndividualTicket> tickets;
    private final List<GroupTicket> groupTickets;

    public TicketManager(TicketFileStorage storage, List<User> users, List<Group> groups) throws IOException {
        this.storage = storage;
        TicketFileStorage.Loaded loaded = storage.load(users, groups);
        this.tickets = new ArrayList<>(loaded.tickets());
        this.groupTickets = new ArrayList<>(loaded.groupTickets());
    }

    public List<IndividualTicket> getTickets() {
        return new ArrayList<>(tickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return new ArrayList<>(groupTickets);
    }

    /** Finds an individual or group ticket by identifier. */
    public Ticket find(String id) {
        for (IndividualTicket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        for (GroupTicket gt : groupTickets) {
            if (gt.getId().equals(id)) {
                return gt;
            }
        }
        return null;
    }

    public String createTicket(String id, User user, LocalDate date, BigDecimal price, LocalDate today) {
        String problem = checkNewTicket(id, date, today);
        if (problem != null) {
            return problem;
        }
        IndividualTicket ticket = new IndividualTicket(id, date, price, user);
        tickets.add(ticket);
        if (!persist()) {
            tickets.remove(ticket);
            return SAVE_FAILED;
        }
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    public String deleteTicket(String id, LocalDate today) {
        Ticket found = find(id);
        if (!(found instanceof IndividualTicket ticket)) {
            return "ERROR: Ticket " + id + " does not exist.";
        }
        GroupTicket owner = ownerOf(ticket);
        if (owner != null) {
            return "ERROR: Ticket " + id + " belongs to group ticket " + owner.getId()
                    + "; use GROUPTICKET DELETE.";
        }
        if (!ticket.isRefundableOn(today)) {
            return "ERROR: Only tickets dated after " + today + " can be deleted.";
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        if (!persist()) {
            tickets.add(index, ticket);
            return SAVE_FAILED;
        }
        return "OK: Ticket deleted: " + id;
    }

    public String createGroupTicket(String id, Group group, LocalDate date, BigDecimal basePrice, LocalDate today) {
        String problem = checkNewTicket(id, date, today);
        if (problem != null) {
            return problem;
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            return "ERROR: Group " + group.getId() + " has no members.";
        }
        List<IndividualTicket> constituents = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            String constituentId = id + "-" + (i + 1);
            if (find(constituentId) != null) {
                return "ERROR: Ticket " + constituentId + " already exists.";
            }
            constituents.add(new IndividualTicket(constituentId, date, basePrice, members.get(i)));
        }
        BigDecimal total = groupPrice(basePrice, members.size());
        GroupTicket groupTicket = new GroupTicket(id, date, total, group, constituents);
        tickets.addAll(constituents);
        groupTickets.add(groupTicket);
        if (!persist()) {
            tickets.removeAll(constituents);
            groupTickets.remove(groupTicket);
            persist();
            return SAVE_FAILED;
        }
        return "OK GROUP TICKET CREATED -> " + total.toPlainString();
    }

    public String deleteGroupTicket(String id, LocalDate today) {
        Ticket found = find(id);
        if (!(found instanceof GroupTicket groupTicket)) {
            return "ERROR: Group ticket " + id + " does not exist.";
        }
        if (!groupTicket.isRefundableOn(today)) {
            return "ERROR: Only group tickets dated after " + today + " can be deleted.";
        }
        List<IndividualTicket> beforeTickets = new ArrayList<>(tickets);
        List<GroupTicket> beforeGroupTickets = new ArrayList<>(groupTickets);
        tickets.removeAll(groupTicket.getTickets());
        groupTickets.remove(groupTicket);
        if (!persist()) {
            restore(beforeTickets, beforeGroupTickets);
            return SAVE_FAILED;
        }
        return "OK: Group ticket deleted: " + id + " (" + groupTicket.getTickets().size() + " tickets)";
    }

    /** True if the user holds a ticket (standalone or constituent) dated on or after today. */
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (IndividualTicket t : tickets) {
            if (t.getUser() == user && t.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** True if the group has a group ticket dated on or after today. */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (GroupTicket gt : groupTickets) {
            if (gt.getGroup() == group && gt.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** Sum of the individual prices with the discount for the group size, rounded half-up to cents. */
    public static BigDecimal groupPrice(BigDecimal basePrice, int members) {
        BigDecimal sum = basePrice.multiply(BigDecimal.valueOf(members));
        BigDecimal discount;
        if (members < 3) {
            discount = BigDecimal.ZERO;
        } else if (members <= 5) {
            discount = new BigDecimal("0.10");
        } else if (members <= 10) {
            discount = new BigDecimal("0.20");
        } else {
            discount = new BigDecimal("0.50");
        }
        return sum.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Saves both ticket files; also used to refresh holder snapshots after user data changes. */
    public boolean persist() {
        try {
            storage.save(tickets, groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String checkNewTicket(String id, LocalDate date, LocalDate today) {
        if (!UserValidator.isNotBlank(id)) {
            return "ERROR: Ticket identifier must not be empty.";
        }
        if (find(id) != null) {
            return "ERROR: Ticket " + id + " already exists.";
        }
        if (date.isBefore(today)) {
            return "ERROR: Tickets cannot be bought for a date before " + today + ".";
        }
        return null;
    }

    private GroupTicket ownerOf(IndividualTicket ticket) {
        for (GroupTicket gt : groupTickets) {
            if (gt.getTickets().contains(ticket)) {
                return gt;
            }
        }
        return null;
    }

    private void restore(List<IndividualTicket> beforeTickets, List<GroupTicket> beforeGroupTickets) {
        tickets.clear();
        tickets.addAll(beforeTickets);
        groupTickets.clear();
        groupTickets.addAll(beforeGroupTickets);
        persist();
    }
}
