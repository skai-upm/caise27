package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;

/**
 * Creates and deletes individual and group tickets, answers date-based questions
 * about them, and keeps the repository in sync.
 */
public class TicketController {

    private static TicketController instance;

    /**
     * Creates the single instance for this execution. Fails if it already exists.
     */
    public static synchronized TicketController initialize(TicketRepository repository, UserController users, GroupController groups) throws IOException {
        if (instance != null) {
            throw new IllegalStateException("TicketController is already initialized");
        }
        instance = new TicketController(repository, users, groups);
        return instance;
    }

    public static synchronized TicketController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TicketController is not initialized");
        }
        return instance;
    }

    /**
     * Discards the instance at the end of an execution.
     */
    public static synchronized void reset() {
        instance = null;
    }

    static final BigDecimal SPEED_PASS_RATE = new BigDecimal("0.20");

    private final TicketRepository repository;
    private final List<Ticket> tickets;

    private TicketController(TicketRepository repository, UserController users, GroupController groups)
            throws IOException {
        this.repository = repository;
        this.tickets = repository.load(users::findByDni, groups::find);
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
        // Cascade: first the passes of every individual ticket involved, then those tickets
        // (owned by the group ticket, if any), then the ticket itself; persisted in one write.
        List<IndividualTicket> owned = ticket instanceof GroupTicket gt
                ? gt.getTickets() : List.of((IndividualTicket) ticket);
        Map<IndividualTicket, List<SpeedPass>> speedPasses = new HashMap<>();
        Map<IndividualTicket, SpecialPass> specialPasses = new HashMap<>();
        for (IndividualTicket t : owned) {
            speedPasses.put(t, t.getSpeedPasses());
            specialPasses.put(t, t.getSpecialPass());
            t.getSpeedPasses().forEach(t::removeSpeedPass);
            t.removeSpecialPass();
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        save(() -> {
            tickets.add(index, ticket);
            for (IndividualTicket t : owned) {
                speedPasses.get(t).forEach(t::addSpeedPass);
                if (specialPasses.get(t) != null) {
                    t.setSpecialPass(specialPasses.get(t));
                }
            }
        });
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
     * Every individual ticket: bought directly or owned by a group ticket.
     */
    public List<IndividualTicket> getIndividualTickets() {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                result.add(it);
            } else if (t instanceof GroupTicket gt) {
                result.addAll(gt.getTickets());
            }
        }
        return result;
    }

    // --- personal passes

    public List<SpeedPass> buySpeedPasses(String ticketId, int quantity, LocalDate today) throws OperationException {
        if (quantity != 1 && quantity != 10) {
            throw new OperationException("SpeedPasses are sold in quantities of 1 or 10, not " + quantity);
        }
        IndividualTicket ticket = requirePassTicket(ticketId, today);
        BigDecimal price = ticket.getPrice().multiply(SPEED_PASS_RATE).setScale(2, RoundingMode.HALF_UP);
        int next = nextSpeedPassNumber(ticket);
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SpeedPass pass = new SpeedPass(ticket.getId() + "-SP" + (next + i), price, false);
            ticket.addSpeedPass(pass);
            bought.add(pass);
        }
        save(() -> bought.forEach(ticket::removeSpeedPass));
        return bought;
    }

    public SpecialPass buySpecialPass(String ticketId, LocalDate today) throws OperationException {
        IndividualTicket ticket = requirePassTicket(ticketId, today);
        if (ticket.hasAcquiredSpecialPass()) {
            throw new OperationException("Ticket " + ticketId + " already acquired its Special Pass");
        }
        SpecialPass pass = new SpecialPass(ticket.getId() + "-SPECIAL", ticket.getPrice(), false);
        ticket.setSpecialPass(pass);
        save(ticket::undoSpecialPassPurchase);
        return pass;
    }

    public void refundSpeedPass(String passId, LocalDate today) throws OperationException {
        for (IndividualTicket ticket : getIndividualTickets()) {
            for (SpeedPass pass : ticket.getSpeedPasses()) {
                if (pass.getId().equals(passId)) {
                    requireRefundable(ticket, pass, today);
                    ticket.removeSpeedPass(pass);
                    save(() -> ticket.addSpeedPass(pass));
                    return;
                }
            }
        }
        throw new OperationException("Unknown SpeedPass: " + passId);
    }

    public void refundSpecialPass(String passId, LocalDate today) throws OperationException {
        for (IndividualTicket ticket : getIndividualTickets()) {
            SpecialPass pass = ticket.getSpecialPass();
            if (pass != null && pass.getId().equals(passId)) {
                requireRefundable(ticket, pass, today);
                ticket.removeSpecialPass();
                save(() -> ticket.setSpecialPass(pass));
                return;
            }
        }
        throw new OperationException("Unknown Special Pass: " + passId);
    }

    private IndividualTicket requirePassTicket(String ticketId, LocalDate today) throws OperationException {
        Ticket ticket = find(ticketId);
        if (ticket == null) {
            throw new OperationException("Unknown ticket: " + ticketId);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new OperationException("Passes are attached to individual tickets; " + ticketId
                    + " is a group ticket");
        }
        if (!individual.isActiveOn(today)) {
            throw new OperationException("Ticket " + ticketId + " dated " + individual.getDate()
                    + " is before the current date " + today);
        }
        return individual;
    }

    private static void requireRefundable(IndividualTicket ticket, Pass pass, LocalDate today)
            throws OperationException {
        if (pass.isConsumed()) {
            throw new OperationException("Pass " + pass.getId() + " has been used and cannot be refunded");
        }
        if (!ticket.isRefundableOn(today)) {
            throw new OperationException("Pass " + pass.getId() + " belongs to ticket " + ticket.getId() + " dated "
                    + ticket.getDate() + "; only passes of tickets after " + today + " can be refunded");
        }
    }

    private static int nextSpeedPassNumber(IndividualTicket ticket) {
        String prefix = ticket.getId() + "-SP";
        int max = 0;
        for (SpeedPass pass : ticket.getSpeedPasses()) {
            try {
                max = Math.max(max, Integer.parseInt(pass.getId().substring(prefix.length())));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                // identifier not generated by this scheme; ignore
            }
        }
        return max + 1;
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
