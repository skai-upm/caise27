package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.OperationException;
import es.upm.amusementpark.business.model.Admission;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.repository.GroupRepository;
import es.upm.amusementpark.business.repository.TicketRepository;

/**
 * Controller of the park operations on groups, tickets and attractions and of
 * the rules that span several entities. Date and price come from the injected
 * {@link SystemController}; users from the injected {@link UserController}.
 * Each successful change is persisted; rejected operations leave state
 * unchanged. One instance per execution.
 */
public final class ParkController {

    private static ParkController instance;

    private final SystemController system;
    private final UserController users;
    private final GroupRepository groupRepository;
    private final TicketRepository ticketRepository;
    private final AttractionRepository attractionRepository;
    private final List<Group> groups;
    private final List<Ticket> tickets;
    private final List<Attraction<?>> attractions;

    private ParkController(SystemController system, UserController users, GroupRepository groupRepository,
                           TicketRepository ticketRepository, AttractionRepository attractionRepository)
            throws IOException {
        this.system = system;
        this.users = users;
        this.groupRepository = groupRepository;
        this.ticketRepository = ticketRepository;
        this.attractionRepository = attractionRepository;

        Map<String, User> usersByDni = new HashMap<>();
        for (User user : users.getUsers()) {
            usersByDni.put(user.getDni(), user);
        }
        this.groups = new ArrayList<>(groupRepository.load(usersByDni));
        Map<String, Group> groupsById = new HashMap<>();
        for (Group group : groups) {
            groupsById.put(group.getId(), group);
        }
        this.tickets = new ArrayList<>(ticketRepository.load(usersByDni, groupsById));
        this.attractions = new ArrayList<>(attractionRepository.load());
    }

    /** Creates the execution's instance with its collaborators and repositories, loading their data. */
    public static synchronized ParkController initialize(SystemController system, UserController users,
                                                         GroupRepository groupRepository,
                                                         TicketRepository ticketRepository,
                                                         AttractionRepository attractionRepository)
            throws IOException {
        if (instance != null) {
            throw new IllegalStateException("ParkController is already initialized");
        }
        instance = new ParkController(system, users, groupRepository, ticketRepository, attractionRepository);
        return instance;
    }

    public static synchronized ParkController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ParkController is not initialized");
        }
        return instance;
    }

    /** Ends the execution's instance so a new execution can initialize its own. */
    public static synchronized void release() {
        instance = null;
    }

    // ---- users ----

    /** Deletes an authenticated user who holds no current or future ticket. */
    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        for (Ticket ticket : tickets) {
            if (ticket.isActiveOn(system.getCurrentDate()) && ticket.getUsers().contains(user)) {
                throw new OperationException("user " + email + " has ticket " + ticket.getId()
                        + " dated " + ticket.getDate() + " and cannot be deleted");
            }
        }
        // Refresh the holder data kept with historical tickets before the user disappears.
        save(() -> ticketRepository.save(tickets), () -> { });
        List<Group> memberOf = new ArrayList<>();
        for (Group group : groups) {
            if (group.hasMember(user)) {
                memberOf.add(group);
                group.removeMember(user);
            }
        }
        Runnable restoreGroups = () -> memberOf.forEach(group -> group.addMember(user));
        save(() -> groupRepository.save(groups), restoreGroups);
        try {
            users.remove(user);
        } catch (OperationException e) {
            restoreGroups.run();
            save(() -> groupRepository.save(groups), () -> { });
            throw e;
        }
        for (Attraction<?> attraction : attractions) {
            attraction.removeVisitor(user);
        }
    }

    // ---- groups ----

    public Group findGroup(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public void createGroup(String id, String name) {
        if (findGroup(id) != null) {
            throw new OperationException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        save(() -> groupRepository.save(groups), () -> groups.remove(group));
    }

    public void addMember(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        User user = existingUser(email);
        if (group.hasMember(user)) {
            throw new OperationException("user " + email + " is already in group " + groupId);
        }
        group.addMember(user);
        save(() -> groupRepository.save(groups), () -> group.removeMember(user));
    }

    public void removeMember(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        User user = users.findByEmail(email);
        if (user == null || !group.hasMember(user)) {
            throw new OperationException("user " + email + " is not in group " + groupId);
        }
        List<User> before = new ArrayList<>(group.getMembers());
        group.removeMember(user);
        save(() -> groupRepository.save(groups), () -> {
            before.forEach(group::removeMember);
            before.forEach(group::addMember);
        });
    }

    public void deleteGroup(String groupId) {
        Group group = unlockedGroup(groupId);
        int index = groups.indexOf(group);
        groups.remove(index);
        save(() -> groupRepository.save(groups), () -> groups.add(index, group));
    }

    /** A group is locked while it has a group ticket dated on or after the system date. */
    public boolean isLocked(Group group) {
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && ticket.isActiveOn(system.getCurrentDate())) {
                return true;
            }
        }
        return false;
    }

    private Group existingGroup(String groupId) {
        Group group = findGroup(groupId);
        if (group == null) {
            throw new OperationException("group " + groupId + " does not exist");
        }
        return group;
    }

    private Group unlockedGroup(String groupId) {
        Group group = existingGroup(groupId);
        if (isLocked(group)) {
            throw new OperationException("group " + groupId + " has a current or future group ticket and is locked");
        }
        return group;
    }

    private User existingUser(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new OperationException("user " + email + " does not exist");
        }
        return user;
    }

    // ---- tickets ----

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    public Ticket findTicket(String id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    private boolean isTicketIdUsed(String id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return true;
            }
            if (ticket instanceof GroupTicket groupTicket) {
                for (IndividualTicket constituent : groupTicket.getTickets()) {
                    if (constituent.getId().equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void requirePurchasable(String id, LocalDate date) {
        if (isTicketIdUsed(id)) {
            throw new OperationException("ticket " + id + " already exists");
        }
        if (date.isBefore(system.getCurrentDate())) {
            throw new OperationException("cannot buy a ticket for " + date + ", before the system date " + system.getCurrentDate());
        }
    }

    public IndividualTicket createTicket(String id, String email, LocalDate date) {
        User user = existingUser(email);
        requirePurchasable(id, date);
        IndividualTicket ticket = new IndividualTicket(id, date, system.getBasePrice(), user);
        tickets.add(ticket);
        save(() -> ticketRepository.save(tickets), () -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new OperationException(isTicketIdUsed(id)
                    ? "ticket " + id + " belongs to a group ticket; use GROUPTICKET DELETE"
                    : "ticket " + id + " does not exist");
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new OperationException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        removeRefundable(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        Group group = existingGroup(groupId);
        int members = group.getMembers().size();
        if (members == 0) {
            throw new OperationException("group " + groupId + " has no members");
        }
        requirePurchasable(id, date);
        for (int position = 1; position <= members; position++) {
            String constituentId = GroupTicket.constituentId(id, position);
            if (isTicketIdUsed(constituentId)) {
                throw new OperationException("ticket " + constituentId + " already exists");
            }
        }
        GroupTicket ticket = new GroupTicket(id, group, date, system.getBasePrice());
        tickets.add(ticket);
        save(() -> ticketRepository.save(tickets), () -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = findTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationException("group ticket " + id + " does not exist");
        }
        removeRefundable(ticket);
    }

    private void removeRefundable(Ticket ticket) {
        if (!ticket.isRefundableOn(system.getCurrentDate())) {
            throw new OperationException("ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + "; only tickets after the system date " + system.getCurrentDate() + " can be deleted");
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        save(() -> ticketRepository.save(tickets), () -> tickets.add(index, ticket));
    }

    // ---- passes ----

    /** Every individual ticket: the ones bought directly and the constituents of group tickets. */
    public List<IndividualTicket> getIndividualTickets() {
        List<IndividualTicket> individuals = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof IndividualTicket individual) {
                individuals.add(individual);
            } else if (ticket instanceof GroupTicket groupTicket) {
                individuals.addAll(groupTicket.getTickets());
            }
        }
        return individuals;
    }

    /** Finds an individual ticket bought directly or as part of a group ticket. */
    public IndividualTicket findIndividualTicket(String id) {
        for (IndividualTicket ticket : getIndividualTickets()) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    private IndividualTicket ticketForPass(String ticketId) {
        IndividualTicket ticket = findIndividualTicket(ticketId);
        if (ticket == null) {
            throw new OperationException("individual ticket " + ticketId + " does not exist");
        }
        if (ticket.getDate().isBefore(system.getCurrentDate())) {
            throw new OperationException("ticket " + ticketId + " is dated " + ticket.getDate()
                    + ", before the system date " + system.getCurrentDate());
        }
        return ticket;
    }

    /** Buys 1 or 10 SpeedPasses for an individual ticket that is not in the past. */
    public List<SpeedPass> buySpeedPasses(String ticketId, int quantity) {
        if (quantity != 1 && quantity != 10) {
            throw new OperationException("SpeedPasses are sold individually or in packs of 10");
        }
        IndividualTicket ticket = ticketForPass(ticketId);
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SpeedPass pass = new SpeedPass(ticket.nextSpeedPassId(), SpeedPass.priceFor(ticket), false);
            ticket.addSpeedPass(pass);
            bought.add(pass);
        }
        save(() -> ticketRepository.save(tickets), () -> bought.forEach(ticket::removeSpeedPass));
        return bought;
    }

    /** Buys the only Special Pass an individual ticket can ever have. */
    public SpecialPass buySpecialPass(String ticketId) {
        IndividualTicket ticket = ticketForPass(ticketId);
        if (ticket.isSpecialPassIssued()) {
            throw new OperationException("ticket " + ticketId + " already had a Special Pass");
        }
        SpecialPass pass = new SpecialPass(ticket.specialPassId(), ticket.getPrice(), false);
        ticket.issueSpecialPass(pass);
        save(() -> ticketRepository.save(tickets), ticket::revokeSpecialPass);
        return pass;
    }

    /** Refunds an unused SpeedPass whose ticket is dated after the system date. */
    public void refundSpeedPass(String passId) {
        for (IndividualTicket ticket : getIndividualTickets()) {
            for (SpeedPass pass : ticket.getSpeedPasses()) {
                if (pass.getId().equals(passId)) {
                    requireRefundable(ticket, pass);
                    List<SpeedPass> before = new ArrayList<>(ticket.getSpeedPasses());
                    ticket.removeSpeedPass(pass);
                    save(() -> ticketRepository.save(tickets), () -> {
                        before.forEach(ticket::removeSpeedPass);
                        before.forEach(ticket::addSpeedPass);
                    });
                    return;
                }
            }
        }
        throw new OperationException("SpeedPass " + passId + " does not exist");
    }

    /** Refunds an unused Special Pass whose ticket is dated after the system date. */
    public void refundSpecialPass(String passId) {
        for (IndividualTicket ticket : getIndividualTickets()) {
            SpecialPass pass = ticket.getSpecialPass();
            if (pass != null && pass.getId().equals(passId)) {
                requireRefundable(ticket, pass);
                ticket.removeSpecialPass();
                save(() -> ticketRepository.save(tickets), () -> {
                    ticket.revokeSpecialPass();
                    ticket.issueSpecialPass(pass);
                });
                return;
            }
        }
        throw new OperationException("Special Pass " + passId + " does not exist");
    }

    private void requireRefundable(IndividualTicket ticket, Pass pass) {
        if (pass.isConsumed()) {
            throw new OperationException(pass.getKind() + " " + pass.getId() + " has been used and cannot be refunded");
        }
        if (!ticket.isRefundableOn(system.getCurrentDate())) {
            throw new OperationException(pass.getKind() + " " + pass.getId() + " belongs to ticket " + ticket.getId()
                    + " dated " + ticket.getDate() + "; only passes of tickets after the system date "
                    + system.getCurrentDate() + " can be refunded");
        }
    }

    // ---- attractions ----

    public Attraction<?> findAttraction(String id) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    public void createAttraction(String id, String name, int capacity, AttractionType type) {
        if (findAttraction(id) != null) {
            throw new OperationException("attraction " + id + " already exists");
        }
        if (capacity <= 0) {
            throw new OperationException("capacity must be a positive integer");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        save(() -> attractionRepository.save(attractions), () -> attractions.remove(attraction));
    }

    /** Admits the users of a ticket valid for the system date; returns how many entered. */
    public int enterAttraction(String attractionId, String ticketId) {
        Attraction<?> attraction = existingAttraction(attractionId);
        Admission admission = existingAdmission(ticketId);
        if (!admission.getDate().equals(system.getCurrentDate())) {
            throw new OperationException("ticket " + ticketId + " is valid for " + admission.getDate()
                    + ", not for the system date " + system.getCurrentDate());
        }
        attraction.admit(admission);
        return admission.getUsers().size();
    }

    /** Lets the present users of a ticket leave, without checking the date; returns how many left. */
    public int exitAttraction(String attractionId, String ticketId) {
        return existingAttraction(attractionId).exit(existingAdmission(ticketId));
    }

    private Attraction<?> existingAttraction(String id) {
        Attraction<?> attraction = findAttraction(id);
        if (attraction == null) {
            throw new OperationException("attraction " + id + " does not exist");
        }
        return attraction;
    }

    /** A ticket usable at attractions: a direct or group ticket, or a group-ticket constituent. */
    private Admission existingAdmission(String id) {
        Ticket ticket = findTicket(id);
        if (ticket != null) {
            return ticket;
        }
        IndividualTicket constituent = findIndividualTicket(id);
        if (constituent == null) {
            throw new OperationException("ticket " + id + " does not exist");
        }
        return constituent;
    }

    // ---- persistence ----

    @FunctionalInterface
    private interface SaveAction {
        void run() throws IOException;
    }

    private static void save(SaveAction action, Runnable undo) {
        try {
            action.run();
        } catch (IOException e) {
            undo.run();
            throw new OperationException("could not save data: " + e.getMessage());
        }
    }
}
