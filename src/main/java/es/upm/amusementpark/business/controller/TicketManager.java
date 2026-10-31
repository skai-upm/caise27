package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.repository.TicketRepository;
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

    private static final SingletonSlot<TicketManager> INSTANCE = new SingletonSlot<>("TicketManager");

    /** Creates the single instance for this execution with its injected dependencies. */
    public static TicketManager initialize(TicketRepository repository, UserManager users, GroupManager groups) throws IOException {
        return INSTANCE.initialize(() -> new TicketManager(repository, users::findByDni, groups::find));
    }

    /** Returns the instance created by {@link #initialize}. */
    public static TicketManager getInstance() {
        return INSTANCE.get();
    }

    /** Discards the instance at the end of an execution. */
    public static void reset() {
        INSTANCE.reset();
    }

    private static final int PRICE_SCALE = 2;

    private final TicketRepository repository;
    private final List<IndividualTicket> individualTickets;
    private final List<GroupTicket> groupTickets;

    private TicketManager(TicketRepository repository, Function<String, User> userByDni,
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

    /** Pass and the individual ticket that owns it. */
    public record OwnedPass(IndividualTicket ticket, Pass pass) {
    }

    private static final BigDecimal SPEED_PASS_RATE = new BigDecimal("0.20");
    private static final String SPEED_SUFFIX = "-SPEED";

    /** Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket's price. */
    public List<SpeedPass> buySpeedPasses(String ticketId, String quantity, LocalDate today) throws IOException {
        IndividualTicket ticket = passHolder(ticketId, today);
        require(quantity.equals("1") || quantity.equals("10"), "SpeedPass quantity must be 1 or 10");
        int count = Integer.parseInt(quantity);
        BigDecimal price = scaled(ticket.getPrice().multiply(SPEED_PASS_RATE));
        int next = nextSpeedPassNumber(ticket);
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SpeedPass pass = new SpeedPass(ticket.getId() + SPEED_SUFFIX + (next + i), price, false);
            ticket.addSpeedPass(pass);
            bought.add(pass);
        }
        try {
            save();
        } catch (IOException e) {
            bought.forEach(ticket::removeSpeedPass);
            throw e;
        }
        return bought;
    }

    /** Buys the only Special Pass a ticket may ever have, at the ticket's price. */
    public SpecialPass buySpecialPass(String ticketId, LocalDate today) throws IOException {
        IndividualTicket ticket = passHolder(ticketId, today);
        require(!ticket.isSpecialPassIssued(), "ticket " + ticketId + " already had a Special Pass");
        SpecialPass pass = new SpecialPass(ticket.getId() + "-SPECIAL", ticket.getPrice(), false);
        ticket.setSpecialPass(pass);
        try {
            save();
        } catch (IOException e) {
            ticket.removeSpecialPass();
            throw e;
        }
        return pass;
    }

    /** Refunds an unused SpeedPass whose ticket is dated strictly after {@code today}. */
    public void deleteSpeedPass(String passId, LocalDate today) throws IOException {
        OwnedPass owned = refundablePass(passId, today);
        require(owned.pass() instanceof SpeedPass, "pass " + passId + " is not a SpeedPass");
        SpeedPass pass = (SpeedPass) owned.pass();
        List<SpeedPass> before = List.copyOf(owned.ticket().getSpeedPasses());
        owned.ticket().removeSpeedPass(pass);
        try {
            save();
        } catch (IOException e) {
            before.forEach(owned.ticket()::removeSpeedPass);
            before.forEach(owned.ticket()::addSpeedPass);
            throw e;
        }
    }

    /** Refunds an unused Special Pass whose ticket is dated strictly after {@code today}. */
    public void deleteSpecialPass(String passId, LocalDate today) throws IOException {
        OwnedPass owned = refundablePass(passId, today);
        require(owned.pass() instanceof SpecialPass, "pass " + passId + " is not a Special Pass");
        owned.ticket().removeSpecialPass();
        try {
            save();
        } catch (IOException e) {
            owned.ticket().setSpecialPass((SpecialPass) owned.pass());
            throw e;
        }
    }

    /** Persists pass consumption after an attraction entry. */
    public void savePassStates() throws IOException {
        save();
    }

    /** Finds a pass by id among every individual ticket, or returns {@code null}. */
    public OwnedPass findPass(String passId) {
        for (IndividualTicket ticket : individualTicketsIncludingConstituents()) {
            for (Pass pass : ticket.getPasses()) {
                if (pass.getId().equals(passId)) {
                    return new OwnedPass(ticket, pass);
                }
            }
        }
        return null;
    }

    /** Every individual ticket of the user: stand-alone ones and group constituents. */
    public List<IndividualTicket> ticketsHeldBy(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket ticket : individualTicketsIncludingConstituents()) {
            if (ticket.getUser() == user) {
                result.add(ticket);
            }
        }
        return result;
    }

    private IndividualTicket passHolder(String ticketId, LocalDate today) {
        Ticket ticket = get(ticketId);
        require(ticket instanceof IndividualTicket, "passes can only be bought for individual tickets");
        require(ticket.isActiveOn(today), "ticket " + ticketId + " is dated before the system date " + today);
        return (IndividualTicket) ticket;
    }

    private OwnedPass refundablePass(String passId, LocalDate today) {
        OwnedPass owned = findPass(passId);
        require(owned != null, "pass " + passId + " does not exist");
        require(!owned.pass().isConsumed(), "pass " + passId + " has already been used");
        require(owned.ticket().isRefundableOn(today),
                "ticket " + owned.ticket().getId() + " is not dated after the system date " + today);
        return owned;
    }

    private static int nextSpeedPassNumber(IndividualTicket ticket) {
        int highest = 0;
        String prefix = ticket.getId() + SPEED_SUFFIX;
        for (SpeedPass pass : ticket.getSpeedPasses()) {
            highest = Math.max(highest, Integer.parseInt(pass.getId().substring(prefix.length())));
        }
        return highest + 1;
    }

    private List<IndividualTicket> individualTicketsIncludingConstituents() {
        List<IndividualTicket> all = new ArrayList<>(individualTickets);
        for (GroupTicket groupTicket : groupTickets) {
            all.addAll(groupTicket.getTickets());
        }
        return all;
    }

    /** Stand-alone individual tickets bought for the user (group constituents excluded). */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket ticket : individualTickets) {
            if (ticket.getUser() == user) {
                result.add(ticket);
            }
        }
        return result;
    }

    /** Group tickets bought by the group. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (GroupTicket ticket : groupTickets) {
            if (ticket.getGroup() == group) {
                result.add(ticket);
            }
        }
        return result;
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
