package es.upm.etsisi.amusementpark.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.PassBook;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.repository.Repositories;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;
import es.upm.etsisi.amusementpark.business.user.UserField;
import es.upm.etsisi.amusementpark.business.user.UserManager;

/**
 * The single central park controller: owns users, groups, tickets, passes and
 * attractions, the current system date and base price, and enforces the rules that
 * relate them. Repositories are injected through {@link #configure(Repositories)}.
 * Lock states are always derived from the tickets and the current date.
 */
public final class ParkController implements PassBook {
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");
    private static final int SINGLE_SPEEDPASS = 1;
    private static final int SPEEDPASS_PACK = 10;

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal basePrice = DEFAULT_PRICE;
    private Repositories repositories;
    private UserManager users;
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Attraction<?>> attractions = new ArrayList<>();
    private final List<Pass> passes = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repositories to use, replaces the whole park state with the data
     * they hold, and resets the system date and price to their start values.
     */
    public void configure(Repositories injected) throws IOException {
        UserManager loadedUsers = new UserManager(injected.users());
        Map<String, User> usersByDni = new HashMap<>();
        loadedUsers.getUsers().forEach(u -> usersByDni.put(u.getDni(), u));
        List<Group> loadedGroups = injected.groups().load(usersByDni);
        Map<String, Group> groupsById = new HashMap<>();
        loadedGroups.forEach(g -> groupsById.put(g.getIdentifier(), g));
        List<Ticket> loadedTickets = injected.tickets().load(usersByDni, groupsById);
        List<Attraction<?>> loadedAttractions = injected.attractions().load();
        Map<String, IndividualTicket> individualById = new HashMap<>();
        for (Ticket t : loadedTickets) {
            t.getEntryTickets().forEach(it -> individualById.put(it.getIdentifier(), it));
        }
        List<Pass> loadedPasses = injected.passes().load(individualById);

        repositories = injected;
        users = loadedUsers;
        replace(groups, loadedGroups);
        replace(tickets, loadedTickets);
        replace(attractions, loadedAttractions);
        replace(passes, loadedPasses);
        currentDate = LocalDate.now(ZoneId.systemDefault());
        basePrice = DEFAULT_PRICE;
        warnings.clear();
        warnings.addAll(injected.users().getWarnings());
        warnings.addAll(injected.groups().getWarnings());
        warnings.addAll(injected.tickets().getWarnings());
        warnings.addAll(injected.attractions().getWarnings());
        warnings.addAll(injected.passes().getWarnings());
    }

    private static <T> void replace(List<T> target, List<T> source) {
        target.clear();
        target.addAll(source);
    }

    public List<String> getLoadWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    // ---- system settings -------------------------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) throws InvalidDataException {
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new InvalidDataException("price must be a positive amount with at most two decimals");
        }
        basePrice = price.setScale(2);
    }

    // ---- users -----------------------------------------------------------------------------

    public UserManager getUsers() {
        return users;
    }

    public void createUser(User user) throws ParkException {
        users.create(user);
    }

    public void updateUser(String email, String password, UserField field, String value) throws ParkException {
        users.update(email, password, field, value);
    }

    /**
     * Deletes a user unless an active ticket or a locked group prevents it. The user is
     * also removed from every group and from any attraction they are still inside.
     */
    public void deleteUser(String email, String password) throws ParkException {
        User user = users.authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new RuleViolationException("user has an active ticket");
        }
        List<Group> memberOf = groups.stream().filter(g -> g.hasMember(user)).toList();
        for (Group g : memberOf) {
            if (isLocked(g)) {
                throw new RuleViolationException("user belongs to group " + g.getIdentifier()
                        + ", which is locked by an active group ticket");
            }
        }
        Map<Group, Integer> positions = new HashMap<>();
        memberOf.forEach(g -> positions.put(g, g.removeMember(user)));
        List<Attraction<?>> inside = attractions.stream().filter(a -> a.isInside(user)).toList();
        inside.forEach(a -> a.release(user));
        Runnable undo = () -> {
            positions.forEach((g, index) -> g.addMember(index, user));
            inside.forEach(a -> a.restore(user));
        };
        persist(undo, this::saveGroups, this::saveTickets);
        try {
            users.remove(user);
        } catch (ParkException e) {
            undo.run();
            bestEffort(this::saveGroups);
            throw e;
        }
    }

    private boolean hasActiveTicket(User user) {
        return tickets.stream()
                .filter(t -> t.isActiveOn(currentDate))
                .anyMatch(t -> t.getUsers().stream().anyMatch(u -> u == user));
    }

    // ---- groups ----------------------------------------------------------------------------

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group findGroup(String identifier) {
        return groups.stream().filter(g -> g.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    /**
     * A group is locked while any of its group tickets is dated today or later.
     */
    public boolean isLocked(Group group) {
        return groupTicketsOf(group).stream().anyMatch(t -> t.isActiveOn(currentDate));
    }

    public void createGroup(String identifier, String name) throws ParkException {
        if (findGroup(identifier) != null) {
            throw new RuleViolationException("group " + identifier + " already exists");
        }
        Group group = new Group(identifier, name);
        groups.add(group);
        persist(() -> groups.remove(group), this::saveGroups);
    }

    public void addToGroup(String identifier, String email) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        User user = requireUser(email);
        if (group.hasMember(user)) {
            throw new RuleViolationException(email + " is already a member of group " + identifier);
        }
        group.addMember(user);
        persist(() -> group.removeMember(user), this::saveGroups);
    }

    public void removeFromGroup(String identifier, String email) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        User user = group.getMembers().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new NotFoundException(email + " is not a member of group " + identifier));
        int index = group.removeMember(user);
        persist(() -> group.addMember(index, user), this::saveGroups);
    }

    public void deleteGroup(String identifier) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        int index = groups.indexOf(group);
        List<GroupTicket> historical = groupTicketsOf(group);
        groups.remove(index);
        historical.forEach(t -> t.setGroup(null));
        persist(() -> {
            groups.add(index, group);
            historical.forEach(t -> t.setGroup(group));
        }, this::saveGroups, this::saveTickets);
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        return tickets.stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(t -> t.getGroup() == group)
                .toList();
    }

    public Group requireGroup(String identifier) throws NotFoundException {
        Group group = findGroup(identifier);
        if (group == null) {
            throw new NotFoundException("no group " + identifier);
        }
        return group;
    }

    private Group requireModifiableGroup(String identifier) throws ParkException {
        Group group = requireGroup(identifier);
        if (isLocked(group)) {
            throw new RuleViolationException("group " + identifier + " is locked by an active group ticket");
        }
        return group;
    }

    public User requireUser(String email) throws NotFoundException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        return user;
    }

    // ---- tickets ---------------------------------------------------------------------------

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    /**
     * Looks a ticket up in the shared identifier space, including group compositions.
     */
    public Ticket findTicket(String identifier) {
        for (Ticket t : tickets) {
            if (t.getIdentifier().equals(identifier)) {
                return t;
            }
            if (t instanceof GroupTicket g) {
                for (IndividualTicket part : g.getComposition()) {
                    if (part.getIdentifier().equals(identifier)) {
                        return part;
                    }
                }
            }
        }
        return null;
    }

    private void requireFreeIdentifier(String identifier) throws RuleViolationException {
        if (findTicket(identifier) != null) {
            throw new RuleViolationException("a ticket with identifier " + identifier + " already exists");
        }
    }

    private void requireNotPast(LocalDate date) throws RuleViolationException {
        if (date.isBefore(currentDate)) {
            throw new RuleViolationException("date " + date + " is before the current date " + currentDate);
        }
    }

    public IndividualTicket createTicket(String identifier, String email, LocalDate date) throws ParkException {
        requireFreeIdentifier(identifier);
        User user = requireUser(email);
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, basePrice, 0);
        tickets.add(ticket);
        persist(() -> tickets.remove(ticket), this::saveTickets);
        return ticket;
    }

    public void deleteTicket(String identifier) throws ParkException {
        Ticket ticket = findTicket(identifier);
        if (ticket == null) {
            throw new NotFoundException("no ticket " + identifier);
        }
        if (ticket instanceof GroupTicket) {
            throw new RuleViolationException(identifier + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (!tickets.contains(ticket)) {
            throw new RuleViolationException(identifier
                    + " belongs to a group ticket and cannot be deleted on its own");
        }
        removeFutureTicket(ticket);
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, LocalDate date)
            throws ParkException {
        requireFreeIdentifier(identifier);
        Group group = requireGroup(groupIdentifier);
        if (group.getMembers().isEmpty()) {
            throw new RuleViolationException("group " + groupIdentifier + " has no members");
        }
        requireNotPast(date);
        List<User> members = group.getMembers();
        for (User member : members) {
            requireFreeIdentifier(GroupTicket.compositionIdentifier(identifier, member));
        }
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = members.stream()
                .map(m -> new IndividualTicket(GroupTicket.compositionIdentifier(identifier, m), date, m,
                        basePrice, discount))
                .toList();
        GroupTicket ticket = new GroupTicket(identifier, date, group, composition);
        tickets.add(ticket);
        persist(() -> tickets.remove(ticket), this::saveTickets);
        return ticket;
    }

    public void deleteGroupTicket(String identifier) throws ParkException {
        if (!(findTicket(identifier) instanceof GroupTicket ticket)) {
            throw new NotFoundException("no group ticket " + identifier);
        }
        removeFutureTicket(ticket);
    }

    /**
     * Deletes a future ticket together with every pass of the individual tickets it
     * covers (a joint refund), or nothing.
     */
    private void removeFutureTicket(Ticket ticket) throws ParkException {
        if (!ticket.getDate().isAfter(currentDate)) {
            throw new RuleViolationException("ticket " + ticket.getIdentifier()
                    + " is for today or earlier and is kept as a historical record");
        }
        List<IndividualTicket> covered = ticket.getEntryTickets();
        List<Pass> previousPasses = new ArrayList<>(passes);
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        passes.removeIf(p -> covered.contains(p.getTicket()));
        persist(() -> {
            tickets.add(index, ticket);
            replace(passes, previousPasses);
        }, this::saveTickets, this::savePasses);
    }

    // ---- passes ----------------------------------------------------------------------------

    public List<Pass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public Pass findPass(String identifier) {
        return passes.stream().filter(p -> p.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    private IndividualTicket requireIndividualTicketForPurchase(String identifier) throws ParkException {
        Ticket ticket = findTicket(identifier);
        if (ticket == null) {
            throw new NotFoundException("no ticket " + identifier);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new RuleViolationException(identifier + " is a group ticket; passes belong to individual tickets");
        }
        requireNotPast(individual.getDate());
        return individual;
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchase>:1..n}; all or none are created.
     */
    public List<SpeedPass> createSpeedPasses(String purchaseIdentifier, String ticketIdentifier, int quantity)
            throws ParkException {
        if (quantity != SINGLE_SPEEDPASS && quantity != SPEEDPASS_PACK) {
            throw new InvalidDataException("SpeedPass quantity must be 1 or 10");
        }
        IndividualTicket ticket = requireIndividualTicketForPurchase(ticketIdentifier);
        List<SpeedPass> created = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = purchaseIdentifier + ":" + n;
            if (findPass(id) != null) {
                throw new RuleViolationException("a pass with identifier " + id + " already exists");
            }
            created.add(new SpeedPass(id, ticket));
        }
        passes.addAll(created);
        persist(() -> passes.removeAll(created), this::savePasses);
        return created;
    }

    public SpecialPass createSpecialPass(String identifier, String ticketIdentifier) throws ParkException {
        if (findPass(identifier) != null) {
            throw new RuleViolationException("a pass with identifier " + identifier + " already exists");
        }
        IndividualTicket ticket = requireIndividualTicketForPurchase(ticketIdentifier);
        if (specialPassOf(ticket).isPresent()) {
            throw new RuleViolationException("ticket " + ticketIdentifier + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(identifier, ticket);
        passes.add(pass);
        persist(() -> passes.remove(pass), this::savePasses);
        return pass;
    }

    public void deleteSpeedPass(String identifier) throws ParkException {
        refund(identifier, SpeedPass.class, "SpeedPass");
    }

    public void deleteSpecialPass(String identifier) throws ParkException {
        refund(identifier, SpecialPass.class, "Special Pass");
    }

    private void refund(String identifier, Class<? extends Pass> kind, String label) throws ParkException {
        Pass pass = findPass(identifier);
        if (!kind.isInstance(pass)) {
            throw new NotFoundException("no " + label + " " + identifier);
        }
        if (pass.isConsumed()) {
            throw new RuleViolationException(label + " " + identifier + " is consumed and cannot be refunded");
        }
        if (!pass.getTicket().getDate().isAfter(currentDate)) {
            throw new RuleViolationException(label + " " + identifier
                    + " belongs to a ticket for today or earlier and cannot be refunded");
        }
        int index = passes.indexOf(pass);
        passes.remove(index);
        persist(() -> passes.add(index, pass), this::savePasses);
    }

    @Override
    public Optional<SpeedPass> unusedSpeedPass(IndividualTicket ticket) {
        return passes.stream()
                .filter(SpeedPass.class::isInstance)
                .map(SpeedPass.class::cast)
                .filter(p -> p.getTicket() == ticket && !p.isConsumed())
                .findFirst();
    }

    @Override
    public Optional<SpecialPass> specialPassOf(IndividualAccess ticket) {
        return passes.stream()
                .filter(SpecialPass.class::isInstance)
                .map(SpecialPass.class::cast)
                .filter(p -> p.getTicket() == ticket)
                .findFirst();
    }

    // ---- attractions -----------------------------------------------------------------------

    public List<Attraction<?>> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction<?> findAttraction(String identifier) {
        return attractions.stream().filter(a -> a.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public Attraction<?> requireAttraction(String identifier) throws NotFoundException {
        Attraction<?> attraction = findAttraction(identifier);
        if (attraction == null) {
            throw new NotFoundException("no attraction " + identifier);
        }
        return attraction;
    }

    public void createAttraction(String identifier, String name, int capacity, AttractionType type)
            throws ParkException {
        if (findAttraction(identifier) != null) {
            throw new RuleViolationException("attraction " + identifier + " already exists");
        }
        if (capacity <= 0) {
            throw new InvalidDataException("capacity must be a positive whole number");
        }
        Attraction<?> attraction = type.create(identifier, name, capacity);
        attractions.add(attraction);
        persist(() -> attractions.remove(attraction), this::saveAttractions);
    }

    /**
     * Admits every user of the ticket together, or nobody, consuming the passes the
     * attraction type requires only when entry succeeds.
     */
    public EntryResult enter(String attractionIdentifier, String ticketIdentifier, boolean speed)
            throws ParkException {
        Attraction<?> attraction = requireAttraction(attractionIdentifier);
        Ticket ticket = findTicket(ticketIdentifier);
        if (ticket == null) {
            throw new NotFoundException("no ticket " + ticketIdentifier);
        }
        for (User user : ticket.getUsers()) {
            if (!users.isRegistered(user)) {
                throw new RuleViolationException(user.getEmail() + " is no longer a registered user");
            }
        }
        EntryResult entry = attraction.enter(ticket, speed, currentDate, this);
        if (!entry.consumedPasses().isEmpty()) {
            persist(() -> attraction.undo(entry), this::savePasses);
        }
        return entry;
    }

    public void exit(String attractionIdentifier, String email) throws ParkException {
        Attraction<?> attraction = requireAttraction(attractionIdentifier);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new NotFoundException(email + " is not inside " + attractionIdentifier));
        attraction.release(user);
    }

    // ---- persistence -----------------------------------------------------------------------

    @FunctionalInterface
    private interface Save {
        void run() throws IOException;
    }

    private void saveGroups() throws IOException {
        repositories.groups().save(groups);
    }

    private void saveTickets() throws IOException {
        repositories.tickets().save(tickets);
    }

    private void saveAttractions() throws IOException {
        repositories.attractions().save(attractions);
    }

    private void savePasses() throws IOException {
        repositories.passes().save(passes);
    }

    /**
     * Saves the given data; if any save fails, reverts the in-memory change, rewrites
     * the previous state of that data and rejects the operation.
     */
    private static void persist(Runnable undo, Save... saves) throws StorageException {
        try {
            for (Save save : saves) {
                save.run();
            }
        } catch (IOException e) {
            undo.run();
            for (Save save : saves) {
                bestEffort(save);
            }
            throw new StorageException("could not save park data: " + e.getMessage());
        }
    }

    private static void bestEffort(Save save) {
        try {
            save.run();
        } catch (IOException ignored) {
            // the original failure is the one reported
        }
    }
}
