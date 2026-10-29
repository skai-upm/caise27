package es.upm.amusementpark.business.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.GeneralAccess;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.ValueParser;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;

/**
 * Single point of central park management: gives access to users (through the injected
 * {@link UserController}), groups, tickets and attractions, and owns the current system date and base
 * ticket price. Repositories are injected at application initialization.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserController users;
    private GroupRepository groupRepository;
    private TicketRepository ticketRepository;
    private AttractionRepository attractionRepository;
    private PassRepository passRepository;

    private Map<String, Group> groups = new LinkedHashMap<>();
    private Map<String, Ticket> tickets = new LinkedHashMap<>();
    private Map<String, Attraction<?>> attractions = new LinkedHashMap<>();

    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal price = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborating user controller and the repositories, reloads the park state from
     * them and resets the system date and price to their start-of-execution values.
     */
    public void configure(UserController userController, GroupRepository groupRepository,
                          TicketRepository ticketRepository, AttractionRepository attractionRepository,
                          PassRepository passRepository) {
        this.users = userController;
        this.groupRepository = groupRepository;
        this.ticketRepository = ticketRepository;
        this.attractionRepository = attractionRepository;
        this.passRepository = passRepository;

        Map<String, User> usersByDni = new HashMap<>();
        for (User u : users.getUsers()) {
            usersByDni.put(u.getDni(), u);
        }
        groups = groupRepository.load(usersByDni);
        tickets = ticketRepository.load(usersByDni, groups);
        attractions = attractionRepository.load();
        attachPasses(passRepository.load(individualTicketsById()));
        currentDate = LocalDate.now(ZoneId.systemDefault());
        price = INITIAL_PRICE;
    }

    // ---------- System state ----------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price.signum() <= 0) {
            throw new ValidationException("price must be positive");
        }
        this.price = price.setScale(2);
    }

    // ---------- Users ----------

    public UserController getUserController() {
        return users;
    }

    public void createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) {
        users.create(firstName, surnames, dni, email, password, phone, card);
        saveAll();
    }

    public void updateUser(String email, String password, String field, String value) {
        users.update(email, password, field, value);
        saveAll();
    }

    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        for (Ticket ticket : allTickets()) {
            if (ticket.isActive(currentDate) && ticket.getHolders().contains(user)) {
                throw new OperationNotAllowedException("user has an active ticket: " + ticket.getId());
            }
        }
        for (Group group : groups.values()) {
            if (group.contains(user) && isLocked(group)) {
                throw new OperationNotAllowedException("user belongs to locked group " + group.getId());
            }
        }
        for (Group group : groups.values()) {
            group.remove(user);
        }
        users.remove(user);
        saveAll();
    }

    // ---------- Groups ----------

    public Group getGroup(String id) {
        return groups.get(id);
    }

    public Group requireGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new NotFoundException("group not found: " + id);
        }
        return group;
    }

    public void createGroup(String id, String name) {
        requireNonEmpty(id, "group identifier");
        requireNonEmpty(name, "group name");
        if (groups.containsKey(id)) {
            throw new DuplicateException("group already exists: " + id);
        }
        groups.put(id, new Group(id, name));
        saveAll();
    }

    public void addToGroup(String groupId, String email) {
        Group group = requireModifiableGroup(groupId);
        User user = users.requireByEmail(email);
        if (group.contains(user)) {
            throw new DuplicateException("user already in group " + groupId + ": " + email);
        }
        group.add(user);
        saveAll();
    }

    public void removeFromGroup(String groupId, String email) {
        Group group = requireModifiableGroup(groupId);
        User user = users.findByEmail(email);
        if (user == null || !group.contains(user)) {
            throw new NotFoundException("user not in group " + groupId + ": " + email);
        }
        group.remove(user);
        saveAll();
    }

    public void deleteGroup(String groupId) {
        Group group = requireModifiableGroup(groupId);
        for (GroupTicket gt : groupTicketsOf(group)) {
            gt.detachGroup();
        }
        groups.remove(groupId);
        saveAll();
    }

    /** A group is locked while it has an associated group ticket that is still active. */
    public boolean isLocked(Group group) {
        for (GroupTicket gt : groupTicketsOf(group)) {
            if (gt.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Group tickets bought by the group, found by traversing the tickets (groups hold no tickets). */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                result.add(gt);
            }
        }
        return result;
    }

    // ---------- Tickets ----------

    /** Finds any ticket, including individual tickets inside a group ticket's composition. */
    public Ticket findTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket gt) {
                for (IndividualTicket member : gt.getComposition()) {
                    if (member.getId().equals(id)) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    public IndividualTicket requireIndividualTicket(String id) {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + id);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ValidationException("ticket " + id + " is not an individual ticket");
        }
        return individual;
    }

    public IndividualTicket createTicket(String id, String email, String date) {
        requireNonEmpty(id, "ticket identifier");
        requireFreeTicketId(id);
        User user = users.requireByEmail(email);
        LocalDate day = requireUsableDate(date);
        IndividualTicket ticket = new IndividualTicket(id, day, user, price, 0);
        tickets.put(id, ticket);
        saveAll();
        return ticket;
    }

    /** Deletes a future individual ticket together with all its passes. */
    public void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            if (findTicket(id) != null) {
                throw new OperationNotAllowedException("ticket " + id + " belongs to a group ticket");
            }
            throw new NotFoundException("ticket not found: " + id);
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new OperationNotAllowedException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        requireDeletable(ticket);
        tickets.remove(id);
        saveAll();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) {
        requireNonEmpty(id, "ticket identifier");
        Group group = requireGroup(groupId);
        LocalDate day = requireUsableDate(date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationNotAllowedException("group " + groupId + " is empty");
        }
        requireFreeTicketId(id);
        for (User member : members) {
            requireFreeTicketId(GroupTicket.memberTicketId(id, member));
        }
        GroupTicket ticket = new GroupTicket(id, day, group, members, price);
        tickets.put(id, ticket);
        saveAll();
        return ticket;
    }

    /**
     * Deletes a future group ticket together with its composition and all their passes. Every check
     * happens before the single removal, so the deletion is never partial.
     */
    public void deleteGroupTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new NotFoundException("group ticket not found: " + id);
        }
        requireDeletable(ticket);
        tickets.remove(id);
        saveAll();
    }

    /** Top-level tickets plus the individual tickets inside group tickets. */
    public List<Ticket> allTickets() {
        List<Ticket> all = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            all.add(t);
            if (t instanceof GroupTicket gt) {
                all.addAll(gt.getComposition());
            }
        }
        return all;
    }

    /** Individual tickets bought directly (not as part of a group ticket) for the user. */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t instanceof IndividualTicket it && it.getUser() == user) {
                result.add(it);
            }
        }
        return result;
    }

    /** Every individual ticket of the user, direct or inside a group ticket's composition. */
    public List<IndividualTicket> individualTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : allTickets()) {
            if (t instanceof IndividualTicket it && it.getUser() == user) {
                result.add(it);
            }
        }
        return result;
    }

    // ---------- Passes (storage; purchase rules live in PassController) ----------

    public List<Pass> allPasses() {
        List<Pass> passes = new ArrayList<>();
        for (Ticket t : allTickets()) {
            if (t instanceof IndividualTicket it) {
                passes.addAll(it.getPasses());
            }
        }
        return passes;
    }

    public Pass findPass(String id) {
        for (Pass p : allPasses()) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    private void attachPasses(List<Pass> passes) {
        for (Pass pass : passes) {
            if (pass instanceof SpecialPass special) {
                pass.getTicket().setSpecialPass(special);
            } else if (pass instanceof SpeedPass speed) {
                pass.getTicket().addSpeedPass(speed);
            }
        }
    }

    private Map<String, IndividualTicket> individualTicketsById() {
        Map<String, IndividualTicket> result = new HashMap<>();
        for (Ticket t : allTickets()) {
            if (t instanceof IndividualTicket it) {
                result.put(it.getId(), it);
            }
        }
        return result;
    }

    // ---------- Attractions ----------

    public Attraction<?> getAttraction(String id) {
        return attractions.get(id);
    }

    public Attraction<?> requireAttraction(String id) {
        Attraction<?> attraction = attractions.get(id);
        if (attraction == null) {
            throw new NotFoundException("attraction not found: " + id);
        }
        return attraction;
    }

    public void createAttraction(String id, String name, String capacity, String type) {
        requireNonEmpty(id, "attraction identifier");
        requireNonEmpty(name, "attraction name");
        if (attractions.containsKey(id)) {
            throw new DuplicateException("attraction already exists: " + id);
        }
        int value = ValueParser.parsePositiveInt(capacity, "capacity");
        AttractionType attractionType = AttractionType.fromName(type);
        if (attractionType == null) {
            throw new ValidationException("invalid attraction type: " + type);
        }
        attractions.put(id, attractionType.create(id, name, value));
        saveAll();
    }

    /** Admits the ticket's holders, consuming passes as the attraction requires. */
    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + ticketId);
        }
        EntryResult result = admit(attraction, ticket, speed);
        if (!result.consumedPasses().isEmpty()) {
            saveAll();
        }
        return result;
    }

    private <T extends GeneralAccess> EntryResult admit(Attraction<T> attraction, Ticket ticket, boolean speed) {
        if (!attraction.getAccessType().isInstance(ticket)) {
            throw new OperationNotAllowedException(attraction.getType() + " attraction " + attraction.getId()
                    + " does not accept ticket " + ticket.getId());
        }
        return attraction.enter(attraction.getAccessType().cast(ticket), currentDate, speed);
    }

    public void exitAttraction(String attractionId, String email) {
        requireAttraction(attractionId).exit(email);
    }

    // ---------- Persistence ----------

    public void saveAll() {
        users.save();
        groupRepository.save(groups.values());
        ticketRepository.save(tickets.values());
        attractionRepository.save(attractions.values());
        passRepository.save(allPasses());
    }

    // ---------- Helpers ----------

    private Group requireModifiableGroup(String id) {
        Group group = requireGroup(id);
        if (isLocked(group)) {
            throw new OperationNotAllowedException("group " + id + " has an active group ticket");
        }
        return group;
    }

    private void requireFreeTicketId(String id) {
        if (findTicket(id) != null) {
            throw new DuplicateException("ticket identifier already in use: " + id);
        }
    }

    /** Parses a date that must not be before the current system date. */
    public LocalDate requireUsableDate(String date) {
        LocalDate day = ValueParser.parseDate(date);
        if (day.isBefore(currentDate)) {
            throw new OperationNotAllowedException("date " + date + " is before the current date " + currentDate);
        }
        return day;
    }

    private void requireDeletable(Ticket ticket) {
        if (!ticket.isDeletable(currentDate)) {
            throw new OperationNotAllowedException("ticket " + ticket.getId()
                    + " is for today or a past date and cannot be deleted");
        }
    }

    private static void requireNonEmpty(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(name + " must not be empty");
        }
    }
}
