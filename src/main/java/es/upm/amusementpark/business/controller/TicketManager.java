package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.rules.UserValidator;

import static es.upm.amusementpark.business.rules.Checks.require;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Keeps stand-alone individual tickets and group tickets (which own their
 * constituent tickets). All ticket identifiers share one namespace. Every
 * successful change is persisted; a change that cannot be saved is undone.
 */
public class TicketManager {

    private static final int PRICE_SCALE = 2;

    private final TicketRepository repository;
    private final List<IndividualTicket> individualTickets;
    private final List<GroupTicket> groupTickets;

    public TicketManager(TicketRepository repository, Function<String, User> userByDni,
                         Function<String, Group> groupById) throws IOException {
        this.repository = repository;
        TicketRepository.Tickets loaded = repository.loadAll(userByDni, groupById);
        this.individualTickets = new ArrayList<>(loaded.individual());
        this.groupTickets = new ArrayList<>(loaded.group());
    }

    public List<IndividualTicket> getIndividualTickets() {
        return Collections.unmodifiableList(individualTickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return Collections.unmodifiableList(groupTickets);
    }

    /** Finds any ticket with that id: stand-alone, group or group constituent. */
    public Ticket find(String id) {
        for (Ticket ticket : allTickets()) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    /** Returns the ticket with that id or rejects the operation. */
    public Ticket get(String id) {
        Ticket ticket = find(id);
        require(ticket != null, "ticket " + id + " does not exist");
        return ticket;
    }

    public IndividualTicket createIndividual(String id, User user, LocalDate date, BigDecimal basePrice,
                                             LocalDate today) throws IOException {
        require(UserValidator.isNotBlank(id), "ticket id must not be empty");
        require(find(id) == null, "ticket " + id + " already exists");
        require(!date.isBefore(today), "ticket date " + date + " is before the system date " + today);
        IndividualTicket ticket = new IndividualTicket(id, date, scaled(basePrice), user);
        individualTickets.add(ticket);
        try {
            save();
        } catch (IOException e) {
            individualTickets.remove(ticket);
            throw e;
        }
        return ticket;
    }

    public GroupTicket createGroup(String id, Group group, LocalDate date, BigDecimal basePrice,
                                   LocalDate today) throws IOException {
        require(UserValidator.isNotBlank(id), "ticket id must not be empty");
        require(find(id) == null, "ticket " + id + " already exists");
        require(!date.isBefore(today), "ticket date " + date + " is before the system date " + today);
        List<User> members = group.getMembers();
        require(!members.isEmpty(), "group " + group.getId() + " has no members");

        BigDecimal total = groupPrice(basePrice, members.size());
        BigDecimal unitPrice = total.divide(BigDecimal.valueOf(members.size()), PRICE_SCALE, RoundingMode.HALF_UP);
        List<IndividualTicket> constituents = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            String constituentId = id + "-" + (i + 1);
            require(find(constituentId) == null, "ticket " + constituentId + " already exists");
            constituents.add(new IndividualTicket(constituentId, date, unitPrice, members.get(i)));
        }

        GroupTicket ticket = new GroupTicket(id, date, total, group, constituents);
        groupTickets.add(ticket);
        try {
            save();
        } catch (IOException e) {
            groupTickets.remove(ticket);
            throw e;
        }
        return ticket;
    }

    /** Deletes a stand-alone individual ticket dated strictly after {@code today}. */
    public void deleteIndividual(String id, LocalDate today) throws IOException {
        Ticket ticket = get(id);
        int index = individualTickets.indexOf(ticket);
        require(index >= 0, "ticket " + id + " is not an individual ticket; delete its group ticket instead");
        require(ticket.isRefundableOn(today), "ticket " + id + " is not dated after the system date " + today);
        individualTickets.remove(index);
        try {
            save();
        } catch (IOException e) {
            individualTickets.add(index, (IndividualTicket) ticket);
            throw e;
        }
    }

    /** Deletes a group ticket and its constituents if dated strictly after {@code today}. */
    public void deleteGroup(String id, LocalDate today) throws IOException {
        Ticket ticket = get(id);
        int index = groupTickets.indexOf(ticket);
        require(index >= 0, "ticket " + id + " is not a group ticket");
        require(ticket.isRefundableOn(today), "ticket " + id + " is not dated after the system date " + today);
        groupTickets.remove(index);
        try {
            save();
        } catch (IOException e) {
            groupTickets.add(index, (GroupTicket) ticket);
            throw e;
        }
    }

    /** Whether the user holds any ticket dated on or after {@code today}. */
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (Ticket ticket : allTickets()) {
            if (ticket instanceof IndividualTicket individual
                    && individual.getUser() == user && ticket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the group has a group ticket dated on or after {@code today}. */
    public boolean isLocked(Group group, LocalDate today) {
        for (GroupTicket ticket : groupTickets) {
            if (ticket.getGroup() == group && ticket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** Total price for a group of the given size: base price per member minus the size discount. */
    public static BigDecimal groupPrice(BigDecimal basePrice, int members) {
        BigDecimal gross = basePrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = BigDecimal.ONE.subtract(discount(members));
        return scaled(gross.multiply(factor));
    }

    /** Discount rate by group size: 0% under 3, 10% for 3-5, 20% for 6-10, 50% above 10. */
    public static BigDecimal discount(int members) {
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

    private static BigDecimal scaled(BigDecimal amount) {
        return amount.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private List<Ticket> allTickets() {
        List<Ticket> all = new ArrayList<>(individualTickets);
        for (GroupTicket groupTicket : groupTickets) {
            all.add(groupTicket);
            all.addAll(groupTicket.getTickets());
        }
        return all;
    }

    private void save() throws IOException {
        repository.saveAll(individualTickets, groupTickets);
    }
}
