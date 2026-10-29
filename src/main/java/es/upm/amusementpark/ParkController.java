package es.upm.amusementpark;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single point of central park management: owns users, groups, tickets and attractions together
 * with the current system date and base ticket price. Only one instance exists per execution.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserService userService;
    private GroupRepository groupRepository;
    private TicketRepository ticketRepository;
    private AttractionRepository attractionRepository;

    private Map<String, Group> groups = new LinkedHashMap<>();
    private Map<String, Ticket> tickets = new LinkedHashMap<>();
    private Map<String, Attraction> attractions = new LinkedHashMap<>();

    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal price = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Points every repository at {@code dataDir}, reloads the park state from it and resets
     * the system date and price to their start-of-execution values.
     */
    public void initialize(Path dataDir) {
        userService = new UserService(new UserRepository(dataDir.resolve("users.txt")));
        groupRepository = new GroupRepository(dataDir.resolve("groups.txt"));
        ticketRepository = new TicketRepository(dataDir.resolve("tickets.txt"));
        attractionRepository = new AttractionRepository(dataDir.resolve("attractions.txt"));

        Map<String, User> usersByDni = new HashMap<>();
        for (User u : userService.getUsers()) {
            usersByDni.put(u.getDni(), u);
        }
        groups = groupRepository.load(usersByDni);
        tickets = ticketRepository.load(usersByDni, groups);
        attractions = attractionRepository.load();
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
            throw new IllegalArgumentException("price must be positive");
        }
        this.price = price.setScale(2);
    }

    // ---------- Users ----------

    public UserService getUserService() {
        return userService;
    }

    public void createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) {
        userService.create(firstName, surnames, dni, email, password, phone, card);
        saveAll();
    }

    public void updateUser(String email, String password, String field, String value) {
        userService.update(email, password, field, value);
        saveAll();
    }

    public void deleteUser(String email, String password) {
        User user = userService.authenticate(email, password);
        for (Ticket ticket : allTickets()) {
            if (ticket.isActive(currentDate) && ticket.getHolders().contains(user)) {
                throw new IllegalArgumentException("user has an active ticket: " + ticket.getId());
            }
        }
        for (Group group : groups.values()) {
            if (group.contains(user) && isLocked(group)) {
                throw new IllegalArgumentException("user belongs to locked group " + group.getId());
            }
        }
        for (Group group : groups.values()) {
            group.remove(user);
        }
        userService.remove(user);
        saveAll();
    }

    // ---------- Groups ----------

    public Group getGroup(String id) {
        return groups.get(id);
    }

    public void createGroup(String id, String name) {
        requireNonEmpty(id, "group identifier");
        requireNonEmpty(name, "group name");
        if (groups.containsKey(id)) {
            throw new IllegalArgumentException("group already exists: " + id);
        }
        groups.put(id, new Group(id, name));
        saveAll();
    }

    public void addToGroup(String groupId, String email) {
        Group group = requireModifiableGroup(groupId);
        User user = requireUser(email);
        if (group.contains(user)) {
            throw new IllegalArgumentException("user already in group " + groupId + ": " + email);
        }
        group.add(user);
        saveAll();
    }

    public void removeFromGroup(String groupId, String email) {
        Group group = requireModifiableGroup(groupId);
        User user = userService.findByEmail(email);
        if (user == null || !group.contains(user)) {
            throw new IllegalArgumentException("user not in group " + groupId + ": " + email);
        }
        group.remove(user);
        saveAll();
    }

    public void deleteGroup(String groupId) {
        Group group = requireModifiableGroup(groupId);
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                gt.detachGroup();
            }
        }
        groups.remove(groupId);
        saveAll();
    }

    /** A group is locked while it has an associated group ticket that is still active. */
    public boolean isLocked(Group group) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group && gt.isActive(currentDate)) {
                return true;
            }
        }
        return false;
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

    public IndividualTicket createTicket(String id, String email, String date) {
        requireNonEmpty(id, "ticket identifier");
        requireFreeTicketId(id);
        User user = requireUser(email);
        LocalDate day = requireUsableDate(date);
        IndividualTicket ticket = new IndividualTicket(id, day, user, price, 0);
        tickets.put(id, ticket);
        saveAll();
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            Ticket member = findTicket(id);
            if (member != null) {
                throw new IllegalArgumentException("ticket " + id + " belongs to a group ticket");
            }
            throw new IllegalArgumentException("ticket not found: " + id);
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new IllegalArgumentException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
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
            throw new IllegalArgumentException("group " + groupId + " is empty");
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

    public void deleteGroupTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new IllegalArgumentException("group ticket not found: " + id);
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

    // ---------- Attractions ----------

    public Attraction getAttraction(String id) {
        return attractions.get(id);
    }

    public void createAttraction(String id, String name, String capacity) {
        requireNonEmpty(id, "attraction identifier");
        requireNonEmpty(name, "attraction name");
        if (attractions.containsKey(id)) {
            throw new IllegalArgumentException("attraction already exists: " + id);
        }
        int value = ValueParser.parsePositiveInt(capacity, "capacity");
        attractions.put(id, new Attraction(id, name, value));
        saveAll();
    }

    /** Admits the ticket's holders to the attraction; returns the users that entered. */
    public List<User> enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("ticket not found: " + ticketId);
        }
        if (!ticket.isValidOn(currentDate)) {
            throw new IllegalArgumentException("ticket " + ticketId + " is not valid on " + currentDate);
        }
        List<User> holders = ticket.getHolders();
        attraction.enter(holders);
        return holders;
    }

    public void exitAttraction(String attractionId, String email) {
        requireAttraction(attractionId).exit(email);
    }

    public Attraction requireAttraction(String id) {
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new IllegalArgumentException("attraction not found: " + id);
        }
        return attraction;
    }

    // ---------- Persistence ----------

    private void saveAll() {
        userService.save();
        groupRepository.save(groups.values());
        ticketRepository.save(tickets.values());
        attractionRepository.save(attractions.values());
    }

    // ---------- Helpers ----------

    private User requireUser(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("user not found: " + email);
        }
        return user;
    }

    private Group requireGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new IllegalArgumentException("group not found: " + id);
        }
        return group;
    }

    private Group requireModifiableGroup(String id) {
        Group group = requireGroup(id);
        if (isLocked(group)) {
            throw new IllegalArgumentException("group " + id + " has an active group ticket");
        }
        return group;
    }

    private void requireFreeTicketId(String id) {
        if (findTicket(id) != null) {
            throw new IllegalArgumentException("ticket identifier already in use: " + id);
        }
    }

    private LocalDate requireUsableDate(String date) {
        LocalDate day = ValueParser.parseDate(date);
        if (day.isBefore(currentDate)) {
            throw new IllegalArgumentException("date " + date + " is before the current date " + currentDate);
        }
        return day;
    }

    private void requireDeletable(Ticket ticket) {
        if (!ticket.isDeletable(currentDate)) {
            throw new IllegalArgumentException("ticket " + ticket.getId()
                    + " is for today or a past date and cannot be deleted");
        }
    }

    private static void requireNonEmpty(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }
}
