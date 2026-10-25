package com.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller of the application. Owns the single system date, the current ticket price
 * and every collection, and applies the rules that span several entities.
 */
public class Park {
    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_PRICE;

    private final UserManager userManager;
    private final GroupRepository groupRepository;
    private final TicketRepository ticketRepository;
    private final GroupTicketRepository groupTicketRepository;
    private final AttractionRepository attractionRepository;
    private final List<Group> groups;
    private final List<IndividualTicket> tickets;
    private final List<GroupTicket> groupTickets;
    private final List<Attraction> attractions;

    /**
     * Loads the park's data from the files in {@code dataDir}.
     */
    public Park(Path dataDir, LocalDate startDate) throws IOException {
        this.currentDate = startDate;
        this.userManager = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
        this.groupRepository = new GroupRepository(dataDir.resolve("groups.txt"));
        this.ticketRepository = new TicketRepository(dataDir.resolve("tickets.txt"));
        this.groupTicketRepository = new GroupTicketRepository(dataDir.resolve("group_tickets.txt"));
        this.attractionRepository = new AttractionRepository(dataDir.resolve("attractions.txt"));
        List<User> users = userManager.getUsers();
        this.groups = groupRepository.load(users);
        this.groupTickets = groupTicketRepository.load(users, groups);
        this.tickets = ticketRepository.load(users);
        this.attractions = attractionRepository.load();
    }

    // ---- System settings ----

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public String setDate(String text) {
        LocalDate date = Validator.parseDate(text);
        if (date == null) {
            return "ERROR: invalid date, expected yyyy-MM-dd";
        }
        currentDate = date;
        return "OK: system date set to " + date;
    }

    public String setPrice(String text) {
        BigDecimal price = Validator.parsePrice(text);
        if (price == null) {
            return "ERROR: invalid price, expected a positive amount with up to two decimals";
        }
        ticketPrice = price.setScale(2, RoundingMode.HALF_UP);
        return "OK: ticket price set to " + format(ticketPrice);
    }

    // ---- Users ----

    public UserManager getUserManager() {
        return userManager;
    }

    public String createUser(String name, String surname, String dni, String email,
                             String password, String phone, String card) {
        return userManager.create(name, surname, dni, email, password, phone, card);
    }

    public String updateUser(String email, String password, String field, String value) {
        return userManager.update(email, password, field, value);
    }

    public String deleteUser(String email, String password) {
        User user = userManager.authenticate(email, password);
        if (user == null) {
            return "ERROR: unknown email or wrong password";
        }
        for (IndividualTicket ticket : allIndividualTickets()) {
            if (ticket.getUser() == user && ticket.isActiveOn(currentDate)) {
                return "ERROR: user has a ticket dated on or after " + currentDate + " (" + ticket.getId() + ")";
            }
        }
        String result = userManager.delete(email, password);
        if (!result.startsWith("OK")) {
            return result;
        }
        boolean member = false;
        for (Group group : groups) {
            member |= group.remove(user);
        }
        if (member && !saveGroups()) {
            return "ERROR: user deleted but groups could not be saved";
        }
        return result;
    }

    // ---- Groups ----

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public String createGroup(String id, String name) {
        if (!Validator.isValidId(id)) {
            return "ERROR: invalid group id";
        }
        if (name.isBlank()) {
            return "ERROR: group name is required";
        }
        if (findGroup(id) != null) {
            return "ERROR: group " + id + " already exists";
        }
        Group group = new Group(id, name);
        groups.add(group);
        if (!saveGroups()) {
            groups.remove(group);
            return "ERROR: could not save groups";
        }
        return "OK: group created";
    }

    public String addToGroup(String id, String email) {
        Group group = findGroup(id);
        if (group == null) {
            return "ERROR: unknown group " + id;
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            return "ERROR: unknown user " + email;
        }
        if (isLocked(group)) {
            return lockedMessage(group);
        }
        if (!group.add(user)) {
            return "ERROR: user is already in group " + id;
        }
        if (!saveGroups()) {
            group.remove(user);
            return "ERROR: could not save groups";
        }
        return "OK: user added to group";
    }

    public String removeFromGroup(String id, String email) {
        Group group = findGroup(id);
        if (group == null) {
            return "ERROR: unknown group " + id;
        }
        User user = userManager.findByEmail(email);
        if (user == null || !group.contains(user)) {
            return "ERROR: user is not in group " + id;
        }
        if (isLocked(group)) {
            return lockedMessage(group);
        }
        group.remove(user);
        if (!saveGroups()) {
            group.add(user);
            return "ERROR: could not save groups";
        }
        return "OK: user removed from group";
    }

    public String deleteGroup(String id) {
        Group group = findGroup(id);
        if (group == null) {
            return "ERROR: unknown group " + id;
        }
        if (isLocked(group)) {
            return lockedMessage(group);
        }
        int index = groups.indexOf(group);
        groups.remove(index);
        if (!saveGroups()) {
            groups.add(index, group);
            return "ERROR: could not save groups";
        }
        return "OK: group deleted";
    }

    /**
     * A group is locked while it has a group ticket dated on or after the system date.
     */
    public boolean isLocked(Group group) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getGroup() == group && groupTicket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    private String lockedMessage(Group group) {
        return "ERROR: group " + group.getId() + " has a group ticket dated on or after " + currentDate;
    }

    private Group findGroup(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    // ---- Tickets ----

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    public String createTicket(String id, String email, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            return "ERROR: unknown user " + email;
        }
        IndividualTicket ticket = new IndividualTicket(id, Validator.parseDate(dateText), ticketPrice, user);
        tickets.add(ticket);
        if (!saveTickets()) {
            tickets.remove(ticket);
            return "ERROR: could not save tickets";
        }
        return "OK TICKET CREATED -> " + format(ticket.getPrice());
    }

    public String deleteTicket(String id) {
        IndividualTicket ticket = null;
        for (IndividualTicket candidate : tickets) {
            if (candidate.getId().equals(id)) {
                ticket = candidate;
            }
        }
        if (ticket == null) {
            return findTicket(id) != null
                    ? "ERROR: ticket " + id + " belongs to a group ticket, use GROUPTICKET DELETE"
                    : "ERROR: unknown ticket " + id;
        }
        if (!ticket.isFutureOn(currentDate)) {
            return "ERROR: only tickets dated after " + currentDate + " can be deleted";
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        if (!saveTickets()) {
            tickets.add(index, ticket);
            return "ERROR: could not save tickets";
        }
        return "OK: ticket deleted";
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        Group group = findGroup(groupId);
        if (group == null) {
            return "ERROR: unknown group " + groupId;
        }
        if (group.getMembers().isEmpty()) {
            return "ERROR: group " + groupId + " has no members";
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, Validator.parseDate(dateText), ticketPrice, group);
        for (IndividualTicket ticket : groupTicket.getTickets()) {
            if (findTicket(ticket.getId()) != null) {
                return "ERROR: ticket id " + ticket.getId() + " already exists";
            }
        }
        groupTickets.add(groupTicket);
        if (!saveGroupTickets()) {
            groupTickets.remove(groupTicket);
            return "ERROR: could not save group tickets";
        }
        return "OK GROUP TICKET CREATED -> " + format(groupTicket.getPrice());
    }

    public String deleteGroupTicket(String id) {
        GroupTicket groupTicket = null;
        for (GroupTicket candidate : groupTickets) {
            if (candidate.getId().equals(id)) {
                groupTicket = candidate;
            }
        }
        if (groupTicket == null) {
            return "ERROR: unknown group ticket " + id;
        }
        if (!groupTicket.isFutureOn(currentDate)) {
            return "ERROR: only group tickets dated after " + currentDate + " can be deleted";
        }
        int index = groupTickets.indexOf(groupTicket);
        groupTickets.remove(index);
        if (!saveGroupTickets()) {
            groupTickets.add(index, groupTicket);
            return "ERROR: could not save group tickets";
        }
        return "OK: group ticket deleted with " + groupTicket.getTickets().size() + " individual tickets";
    }

    /**
     * Finds any ticket by id: individual, group, or generated inside a group ticket.
     */
    public Ticket findTicket(String id) {
        for (IndividualTicket ticket : allIndividualTickets()) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getId().equals(id)) {
                return groupTicket;
            }
        }
        return null;
    }

    private List<IndividualTicket> allIndividualTickets() {
        List<IndividualTicket> all = new ArrayList<>(tickets);
        for (GroupTicket groupTicket : groupTickets) {
            all.addAll(groupTicket.getTickets());
        }
        return all;
    }

    private String checkNewTicket(String id, String dateText) {
        if (!Validator.isValidId(id)) {
            return "ERROR: invalid ticket id";
        }
        if (findTicket(id) != null) {
            return "ERROR: ticket id " + id + " already exists";
        }
        LocalDate date = Validator.parseDate(dateText);
        if (date == null) {
            return "ERROR: invalid date, expected yyyy-MM-dd";
        }
        if (date.isBefore(currentDate)) {
            return "ERROR: tickets cannot be bought for a date before " + currentDate;
        }
        return null;
    }

    // ---- Attractions ----

    public String createAttraction(String id, String name, String capacityText) {
        if (!Validator.isValidId(id)) {
            return "ERROR: invalid attraction id";
        }
        if (name.isBlank()) {
            return "ERROR: attraction name is required";
        }
        int capacity = Validator.parseCapacity(capacityText);
        if (capacity <= 0) {
            return "ERROR: capacity must be a positive integer";
        }
        if (findAttraction(id) != null) {
            return "ERROR: attraction " + id + " already exists";
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        if (!saveAttractions()) {
            attractions.remove(attraction);
            return "ERROR: could not save attractions";
        }
        return "OK: attraction created";
    }

    public String deleteAttraction(String id) {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        if (!attraction.getPresent().isEmpty()) {
            return "ERROR: attraction " + id + " has users present";
        }
        int index = attractions.indexOf(attraction);
        attractions.remove(index);
        if (!saveAttractions()) {
            attractions.add(index, attraction);
            return "ERROR: could not save attractions";
        }
        return "OK: attraction deleted";
    }

    public String enterAttraction(String id, String ticketId) {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            return "ERROR: unknown ticket " + ticketId;
        }
        if (!ticket.isValidOn(currentDate)) {
            return "ERROR: ticket " + ticketId + " is for " + ticket.getDate() + ", not " + currentDate;
        }
        String error = attraction.enter(ticket);
        if (error != null) {
            return "ERROR: " + error;
        }
        return "OK: " + ticket.getUsers().size() + " user(s) entered " + id;
    }

    public String exitAttraction(String id, String ticketId) {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            return "ERROR: unknown ticket " + ticketId;
        }
        int left = attraction.exit(ticket);
        if (left == 0) {
            return "ERROR: no user of ticket " + ticketId + " is in " + id;
        }
        return "OK: " + left + " user(s) left " + id;
    }

    public String attractionStatus(String id) {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        StringBuilder status = new StringBuilder();
        status.append("Attraction ").append(attraction.getId()).append(" - ").append(attraction.getName())
                .append(System.lineSeparator())
                .append("  Capacity: ").append(attraction.getCapacity()).append(System.lineSeparator())
                .append("  Present: ").append(attraction.getPresent().size()).append(System.lineSeparator())
                .append("  Available: ").append(attraction.getAvailable()).append(System.lineSeparator())
                .append("  Users:");
        if (attraction.getPresent().isEmpty()) {
            status.append(" none");
        }
        for (User user : attraction.getPresent()) {
            status.append(System.lineSeparator()).append("    ").append(user.getName()).append(' ')
                    .append(user.getSurname()).append(" (").append(user.getDni()).append(')');
        }
        return status.toString();
    }

    public Attraction findAttraction(String id) {
        for (Attraction attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    // ---- Persistence ----

    private boolean saveGroups() {
        try {
            groupRepository.save(groups);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean saveTickets() {
        try {
            ticketRepository.save(tickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean saveGroupTickets() {
        try {
            groupTicketRepository.save(groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean saveAttractions() {
        try {
            attractionRepository.save(attractions);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    static String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
