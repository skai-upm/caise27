package es.upm.park.business;

import es.upm.park.business.model.Result;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.persistence.AttractionFileStore;
import es.upm.park.persistence.DataDirectory;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.persistence.GroupTicketFileStore;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.persistence.UserFileStore;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main controller of the application. Owns the execution-wide settings (current system date
 * and ticket price), coordinates the managers for rules that span several entities, and
 * assembles managers and stores for a data directory.
 */
public class ParkController implements AutoCloseable {

    /** Ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private final Path temporaryDirectory;
    private final AtomicBoolean closed = new AtomicBoolean();
    private LocalDate currentDate;
    private BigDecimal currentPrice = DEFAULT_PRICE;

    public ParkController(UserManager users, GroupManager groups, TicketManager tickets,
                          AttractionManager attractions, LocalDate initialDate) {
        this(users, groups, tickets, attractions, initialDate, null);
    }

    private ParkController(UserManager users, GroupManager groups, TicketManager tickets,
                           AttractionManager attractions, LocalDate initialDate, Path temporaryDirectory) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.currentDate = initialDate;
        this.temporaryDirectory = temporaryDirectory;
    }

    /** Controller over the data files in {@code dataDir}, starting at the operating system's date. */
    public static ParkController open(Path dataDir) throws IOException {
        return open(dataDir, LocalDate.now());
    }

    public static ParkController open(Path dataDir, LocalDate initialDate) throws IOException {
        return assemble(dataDir, initialDate, null);
    }

    /** Controller over a new, empty run directory under {@code tmpRoot}; {@link #close()} deletes it. */
    public static ParkController openTemporary(Path tmpRoot) throws IOException {
        Path runDir = DataDirectory.createRunDirectory(tmpRoot);
        try {
            return assemble(runDir, LocalDate.now(), runDir);
        } catch (IOException | RuntimeException e) {
            DataDirectory.deleteQuietly(runDir);
            throw e;
        }
    }

    private static ParkController assemble(Path dataDir, LocalDate date, Path temporary) throws IOException {
        UserManager users = new UserManager(new UserFileStore(dataDir.resolve(DataDirectory.USERS_FILE)));
        GroupManager groups = new GroupManager(new GroupFileStore(dataDir.resolve(DataDirectory.GROUPS_FILE)), users);
        TicketManager tickets = new TicketManager(new TicketFileStore(dataDir.resolve(DataDirectory.TICKETS_FILE)),
                new GroupTicketFileStore(dataDir.resolve(DataDirectory.GROUP_TICKETS_FILE)), users, groups);
        AttractionManager attractions = new AttractionManager(
                new AttractionFileStore(dataDir.resolve(DataDirectory.ATTRACTIONS_FILE)));
        return new ParkController(users, groups, tickets, attractions, date, temporary);
    }

    /** The temporary data directory of this controller, if it was opened with {@link #openTemporary}. */
    public Optional<Path> getTemporaryDirectory() {
        return Optional.ofNullable(temporaryDirectory);
    }

    /** Deletes the temporary data directory, if any. Safe to call more than once. */
    @Override
    public void close() {
        if (temporaryDirectory != null && closed.compareAndSet(false, true)) {
            DataDirectory.deleteQuietly(temporaryDirectory);
        }
    }

    public UserManager users() {
        return users;
    }

    public GroupManager groups() {
        return groups;
    }

    public TicketManager tickets() {
        return tickets;
    }

    public AttractionManager attractions() {
        return attractions;
    }

    // ---- system settings ----

    /** The current system date; time-dependent operations must use this, never the real clock. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /** The price that the next ticket will capture. */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public Result setDate(LocalDate date) {
        currentDate = date;
        return Result.ok("System date set to " + currentDate + ".");
    }

    public Result setPrice(BigDecimal price) {
        currentPrice = price;
        return Result.ok("Ticket price set to " + currentPrice.toPlainString() + ".");
    }

    // ---- users ----

    public Result createUser(String firstName, String surnames, String dni, String email,
                             String password, String phone, String card) {
        return users.create(firstName, surnames, dni, email, password, phone, card);
    }

    public Result updateUser(String email, String password, UserField field, String newValue) {
        return users.update(email, password, field, newValue);
    }

    /** Deletes a user unless it holds current/future tickets; it also leaves attractions and groups. */
    public Result deleteUser(String email, String password) {
        Optional<User> target = users.authenticate(email, password);
        if (target.isPresent() && tickets.hasTicketOnOrAfter(target.get(), currentDate)) {
            return Result.error("User " + email + " has tickets dated on or after " + currentDate + " and cannot be deleted.");
        }
        Result result = users.delete(email, password);
        if (result.success() && target.isPresent()) {
            attractions.removeUserEverywhere(target.get());
            Result cleanup = groups.removeUserEverywhere(target.get());
            if (!cleanup.success()) {
                return Result.error(result.message() + " " + cleanup.message());
            }
        }
        return result;
    }

    // ---- groups ----

    public Result createGroup(String id, String name) {
        return groups.create(id, name);
    }

    public Result addToGroup(String id, String email) {
        Result locked = checkGroupUnlocked(id);
        return locked != null ? locked : groups.addMember(id, email);
    }

    public Result removeFromGroup(String id, String email) {
        Result locked = checkGroupUnlocked(id);
        return locked != null ? locked : groups.removeMember(id, email);
    }

    public Result deleteGroup(String id) {
        Result locked = checkGroupUnlocked(id);
        return locked != null ? locked : groups.delete(id);
    }

    /** Error when the group is locked by a current or future group ticket, otherwise {@code null}. */
    private Result checkGroupUnlocked(String id) {
        Optional<Group> target = groups.find(id);
        if (target.isPresent() && tickets.isGroupLocked(target.get(), currentDate)) {
            return Result.error("Group " + id + " has group tickets dated on or after " + currentDate
                    + " and cannot be modified or deleted.");
        }
        return null;
    }

    // ---- tickets ----

    public Result createTicket(String id, String email, LocalDate date) {
        return tickets.create(id, email, date, currentDate, currentPrice);
    }

    public Result deleteTicket(String id) {
        return tickets.delete(id, currentDate);
    }

    public Result createGroupTicket(String id, String groupId, LocalDate date) {
        Optional<Group> group = groups.find(groupId);
        if (group.isEmpty()) {
            return Result.error("Group " + groupId + " does not exist.");
        }
        return tickets.createGroupTicket(id, group.get(), date, currentDate, currentPrice);
    }

    public Result deleteGroupTicket(String id) {
        return tickets.deleteGroupTicket(id, currentDate);
    }

    // ---- attractions ----

    public Result createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    public Result enterAttraction(String attractionId, String ticketId) {
        return attractions.enter(attractionId, tickets.findForAccess(ticketId), ticketId, currentDate);
    }

    public Result exitAttraction(String attractionId, String ticketId) {
        return attractions.exit(attractionId, tickets.findForAccess(ticketId), ticketId);
    }

    public Result attractionStatus(String attractionId) {
        return attractions.status(attractionId);
    }
}
