package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Single central manager of the park state: users, groups, tickets, attractions, the current
 * system date and the current base ticket price. Only one instance exists per execution.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = INITIAL_PRICE;
    private UserService userService;
    private GroupRepository groupRepository;
    private TicketRepository ticketRepository;
    private AttractionRepository attractionRepository;
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Attraction> attractions = new ArrayList<>();

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * (Re)initialises the park from the given storage, with the given current date and the initial price.
     */
    public void load(Storage storage, LocalDate today) {
        currentDate = today;
        currentPrice = INITIAL_PRICE;
        userService = new UserService(new UserRepository(storage.usersFile()));
        groupRepository = new GroupRepository(storage.groupsFile());
        ticketRepository = new TicketRepository(storage.ticketsFile());
        attractionRepository = new AttractionRepository(storage.attractionsFile());
        attractions.clear();
        attractions.addAll(attractionRepository.load());
        groups.clear();
        groups.addAll(groupRepository.load(userService));
        tickets.clear();
        tickets.addAll(ticketRepository.load(userService, groups));
    }

    private void saveAll() {
        userService.save();
        groupRepository.save(groups);
        ticketRepository.save(tickets);
        attractionRepository.save(attractions);
    }

    // ----- system -----

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentDate(String text) throws OperationException {
        currentDate = parseDate(text);
    }

    public void setCurrentPrice(String text) throws OperationException {
        currentPrice = parsePrice(text);
    }

    static LocalDate parseDate(String text) throws OperationException {
        try {
            return LocalDate.parse(text.strip());
        } catch (DateTimeParseException e) {
            throw new OperationException("invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    static BigDecimal parsePrice(String text) throws OperationException {
        String value = text.strip();
        int dot = value.indexOf('.');
        boolean valid = !value.isEmpty() && dot != 0 && dot != value.length() - 1
                && (dot < 0 || value.length() - dot - 1 <= 2);
        for (int i = 0; valid && i < value.length(); i++) {
            char c = value.charAt(i);
            valid = (c >= '0' && c <= '9') || (c == '.' && i == dot);
        }
        if (!valid) {
            throw new OperationException("invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(value).setScale(2);
        if (price.signum() <= 0) {
            throw new OperationException("price must be positive: " + text);
        }
        return price;
    }

    // ----- users -----

    public UserService getUserService() {
        return userService;
    }

    public void createUser(String firstName, String surnames, String dni, String email, String password,
                           String phone, String card) throws OperationException {
        userService.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, String field, String value) throws OperationException {
        userService.update(email, password, field, value);
        saveAll();
    }

    public void deleteUser(String email, String password) throws OperationException {
        User user = userService.authenticate(email, password);
        for (Ticket ticket : tickets) {
            if (ticket.isActive(currentDate) && ticket.getUsers().contains(user)) {
                throw new OperationException("user has an active ticket: " + ticket.getId());
            }
        }
        List<Group> memberships = new ArrayList<>();
        for (Group group : groups) {
            if (group.contains(user)) {
                if (isGroupLocked(group)) {
                    throw new OperationException("user belongs to locked group " + group.getId());
                }
                memberships.add(group);
            }
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        userService.remove(user);
        saveAll();
    }

    // ----- groups -----

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group findGroup(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    private Group requireGroup(String id) throws OperationException {
        Group group = findGroup(id);
        if (group == null) {
            throw new OperationException("group not found: " + id);
        }
        return group;
    }

    private Group requireModifiableGroup(String id) throws OperationException {
        Group group = requireGroup(id);
        if (isGroupLocked(group)) {
            throw new OperationException("group " + id + " is locked by an active group ticket");
        }
        return group;
    }

    private User requireUser(String email) throws OperationException {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new OperationException("user not found: " + email);
        }
        return user;
    }

    /**
     * A group is locked while any group ticket bought by it is dated today or later.
     */
    public boolean isGroupLocked(Group group) {
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group && gt.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public void createGroup(String id, String name) throws OperationException {
        requireIdentifier(id);
        if (findGroup(id) != null) {
            throw new OperationException("group already exists: " + id);
        }
        groups.add(new Group(id, name));
        saveAll();
    }

    public void addToGroup(String groupId, String email) throws OperationException {
        Group group = requireModifiableGroup(groupId);
        User user = requireUser(email);
        if (!group.add(user)) {
            throw new OperationException("user already in group: " + email);
        }
        saveAll();
    }

    public void removeFromGroup(String groupId, String email) throws OperationException {
        Group group = requireModifiableGroup(groupId);
        User user = requireUser(email);
        if (!group.remove(user)) {
            throw new OperationException("user is not in group: " + email);
        }
        saveAll();
    }

    public void deleteGroup(String groupId) throws OperationException {
        Group group = requireModifiableGroup(groupId);
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                gt.detachGroup();
            }
        }
        groups.remove(group);
        saveAll();
    }

    // ----- tickets -----

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    /**
     * Finds any ticket, including individual tickets inside group tickets.
     */
    public Ticket findTicket(String id) {
        for (Ticket ticket : tickets) {
            Ticket found = ticket.findTicket(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private void requireFreeTicketId(String id) throws OperationException {
        requireIdentifier(id);
        if (findTicket(id) != null) {
            throw new OperationException("ticket identifier already in use: " + id);
        }
    }

    private LocalDate requireNotPast(String dateText) throws OperationException {
        LocalDate date = parseDate(dateText);
        if (date.isBefore(currentDate)) {
            throw new OperationException("ticket date is before the current date " + currentDate);
        }
        return date;
    }

    public IndividualTicket createTicket(String id, String email, String dateText) throws OperationException {
        requireFreeTicketId(id);
        User user = requireUser(email);
        LocalDate date = requireNotPast(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, currentPrice, 0, user);
        tickets.add(ticket);
        saveAll();
        return ticket;
    }

    public void deleteTicket(String id) throws OperationException {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new OperationException("ticket not found: " + id);
        }
        if (!tickets.contains(ticket) || !(ticket instanceof IndividualTicket)) {
            throw new OperationException("ticket " + id + " is not an individual ticket; use GROUPTICKET DELETE");
        }
        requireDeletable(ticket);
        tickets.remove(ticket);
        saveAll();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws OperationException {
        requireFreeTicketId(id);
        Group group = requireGroup(groupId);
        LocalDate date = requireNotPast(dateText);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationException("group " + groupId + " has no members");
        }
        for (User member : members) {
            String derived = GroupTicket.componentId(id, member);
            if (findTicket(derived) != null) {
                throw new OperationException("ticket identifier already in use: " + derived);
            }
        }
        GroupTicket ticket = new GroupTicket(id, date, currentPrice, group, members);
        tickets.add(ticket);
        saveAll();
        return ticket;
    }

    public void deleteGroupTicket(String id) throws OperationException {
        Ticket ticket = findTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationException("group ticket not found: " + id);
        }
        requireDeletable(ticket);
        tickets.remove(ticket);
        saveAll();
    }

    private void requireDeletable(Ticket ticket) throws OperationException {
        if (!ticket.getDate().isAfter(currentDate)) {
            throw new OperationException("only tickets dated after " + currentDate + " can be deleted");
        }
    }

    // ----- attractions -----

    public Attraction findAttraction(String id) {
        for (Attraction a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public Attraction requireAttraction(String id) throws OperationException {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            throw new OperationException("attraction not found: " + id);
        }
        return attraction;
    }

    public void createAttraction(String id, String name, String capacityText) throws OperationException {
        requireIdentifier(id);
        if (findAttraction(id) != null) {
            throw new OperationException("attraction already exists: " + id);
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.strip());
        } catch (NumberFormatException e) {
            throw new OperationException("invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new OperationException("capacity must be positive: " + capacityText);
        }
        attractions.add(new Attraction(id, name, capacity));
        saveAll();
    }

    /**
     * Admits the users of a ticket valid today; all of them or none.
     */
    public int enterAttraction(String attractionId, String ticketId) throws OperationException {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            throw new OperationException("ticket not found: " + ticketId);
        }
        if (!ticket.isValidOn(currentDate)) {
            throw new OperationException("ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on " + currentDate);
        }
        List<User> users = ticket.getUsers();
        attraction.enter(users);
        return users.size();
    }

    public void exitAttraction(String attractionId, String email) throws OperationException {
        Attraction attraction = requireAttraction(attractionId);
        User user = attraction.findOccupant(email);
        if (user == null) {
            throw new OperationException("user is not inside attraction " + attractionId + ": " + email);
        }
        attraction.exit(user);
    }

    private static void requireIdentifier(String id) throws OperationException {
        if (id.isBlank()) {
            throw new OperationException("identifier must not be empty");
        }
    }
}
