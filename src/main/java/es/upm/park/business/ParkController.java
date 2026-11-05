package es.upm.park.business;

import es.upm.park.business.model.Result;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main controller of the application (one instance per execution). Owns the execution-wide
 * settings (current system date and ticket price) and coordinates the managers for rules that
 * span several entities. Its collaborators are injected at startup by {@link ApplicationContext}.
 */
public final class ParkController {

    /** Ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private AttractionManager attractions;
    private PassManager passes;
    private InvoiceDirector invoiceDirector;
    private LocalDate currentDate;
    private BigDecimal currentPrice = DEFAULT_PRICE;

    private ParkController() {
    }

    /** The single main controller of the execution. */
    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborating managers and services into the single instance and resets the
     * execution settings (date, default price). Called at startup by {@link ApplicationContext}.
     */
    public static ParkController configure(UserManager users, GroupManager groups, TicketManager tickets,
                                           AttractionManager attractions, PassManager passes,
                                           InvoiceDirector invoiceDirector, LocalDate initialDate) {
        INSTANCE.users = users;
        INSTANCE.groups = groups;
        INSTANCE.tickets = tickets;
        INSTANCE.attractions = attractions;
        INSTANCE.passes = passes;
        INSTANCE.invoiceDirector = invoiceDirector;
        INSTANCE.currentDate = initialDate;
        INSTANCE.currentPrice = DEFAULT_PRICE;
        return INSTANCE;
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

    public PassManager passes() {
        return passes;
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

    /** Deletes a future individual ticket together with its passes (passes first). */
    public Result deleteTicket(String id) {
        Result rejected = tickets.checkDeletable(id, currentDate);
        if (rejected != null) {
            return rejected;
        }
        return cascade(List.of(tickets.find(id).orElseThrow()), () -> tickets.delete(id, currentDate));
    }

    public Result createGroupTicket(String id, String groupId, LocalDate date) {
        Optional<Group> group = groups.find(groupId);
        if (group.isEmpty()) {
            return Result.error("Group " + groupId + " does not exist.");
        }
        return tickets.createGroupTicket(id, group.get(), date, currentDate, currentPrice);
    }

    /** Deletes a future group ticket: passes of its tickets, then the tickets and the group ticket. */
    public Result deleteGroupTicket(String id) {
        Result rejected = tickets.checkGroupDeletable(id, currentDate);
        if (rejected != null) {
            return rejected;
        }
        return cascade(tickets.findGroupTicket(id).orElseThrow().getTickets(),
                () -> tickets.deleteGroupTicket(id, currentDate));
    }

    /** Removes the passes of {@code owners}, then runs the ticket deletion; restores the passes if it fails. */
    private Result cascade(List<IndividualTicket> owners, java.util.function.Supplier<Result> ticketDeletion) {
        PassManager.Removal removal = passes.removePassesOf(owners);
        if (!removal.result().success()) {
            return removal.result();
        }
        Result deleted = ticketDeletion.get();
        if (!deleted.success()) {
            Result restored = passes.restore(removal.removed());
            return restored.success() ? deleted : Result.error(deleted.message() + " " + restored.message());
        }
        int count = removal.removed().size();
        return count == 0 ? deleted : Result.ok(deleted.message() + " " + count + " pass(es) deleted.");
    }

    // ---- passes ----

    public Result buySpeedPasses(String purchaseId, String ticketId, String quantity) {
        return passes.buySpeedPasses(purchaseId, ticketId, quantity, currentDate);
    }

    public Result buySpecialPass(String id, String ticketId) {
        return passes.buySpecialPass(id, ticketId, currentDate);
    }

    public Result refundSpeedPass(String id) {
        return passes.refund(id, SpeedPass.TYPE_KEY, currentDate);
    }

    public Result refundSpecialPass(String id) {
        return passes.refund(id, SpecialPass.TYPE_KEY, currentDate);
    }

    // ---- invoices ----

    /** Invoice of the individual tickets bought directly by the user with this email. */
    public Result createUserInvoice(String email) {
        Optional<User> user = users.findByEmail(email);
        if (user.isEmpty()) {
            return Result.error("No registered user with email " + email + ".");
        }
        User u = user.get();
        Invoice invoice = invoiceDirector.construct(new UserInvoiceBuilder(),
                "USER " + u.getEmail() + " - " + u.getFirstName() + " " + u.getSurnames(), currentDate,
                tickets.getTickets().stream().filter(t -> t.getUser() == u).toList(), List.of(),
                passes.passesOf(u));
        return Result.ok(invoice.format());
    }

    /** Invoice of the group-ticket purchases of the group with this identifier. */
    public Result createGroupInvoice(String groupId) {
        Optional<Group> group = groups.find(groupId);
        if (group.isEmpty()) {
            return Result.error("Group " + groupId + " does not exist.");
        }
        Group g = group.get();
        Invoice invoice = invoiceDirector.construct(new GroupInvoiceBuilder(),
                "GROUP " + g.getId() + " - " + g.getName(), currentDate,
                List.of(), tickets.getGroupTickets().stream().filter(t -> t.getGroup() == g).toList(), List.of());
        return Result.ok(invoice.format());
    }

    // ---- attractions ----

    public Result createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    public Result createAttraction(String id, String name, String capacity, AttractionType type) {
        return attractions.create(id, name, capacity, type);
    }

    public Result enterAttraction(String attractionId, String ticketId) {
        return enterAttraction(attractionId, ticketId, false);
    }

    /**
     * Entry with optional SpeedPass use. Required passes are looked up first, the entry is then
     * performed atomically by the attraction, and only after it succeeded are the passes consumed.
     */
    public Result enterAttraction(String attractionId, String ticketId, boolean speed) {
        Optional<Attraction<?>> attraction = attractions.find(attractionId);
        Optional<Ticket> ticket = tickets.findForAccess(ticketId);
        if (attraction.isEmpty() || ticket.isEmpty()) {
            return attractions.enter(attractionId, ticket, ticketId, currentDate);
        }
        Attraction<?> a = attraction.get();
        if (speed && !a.allowsSpeed()) {
            return Result.error("Attraction " + attractionId + " (" + a.getType() + ") does not accept the SPEED option.");
        }
        List<IndividualTicket> individual = individualTicketsOf(ticket.get());
        List<Pass> required = new ArrayList<>();
        if (speed) {
            for (IndividualTicket t : individual) {
                Optional<Pass> pass = passes.findAvailableSpeedPass(t);
                if (pass.isEmpty()) {
                    return Result.error("Ticket " + t.getId() + " has no unused SpeedPass; nobody entered.");
                }
                required.add(pass.get());
            }
        }
        if (a.requiresSpecialPass() && a.accepts(ticket.get())) {
            Optional<Pass> pass = passes.findAvailableSpecialPass(individual.get(0));
            if (pass.isEmpty()) {
                return Result.error("Ticket " + ticketId + " has no unused Special Pass for attraction " + attractionId + ".");
            }
            required.add(pass.get());
        }
        Result entry = a.enter(ticket.get(), currentDate);
        if (!entry.success() || required.isEmpty()) {
            return entry;
        }
        Result consumed = passes.consume(required);
        if (!consumed.success()) {
            a.exit(ticket.get());
            return consumed;
        }
        String ids = String.join(", ", required.stream().map(Pass::getId).toList());
        String kind = a.requiresSpecialPass() ? "Special Pass consumed: " : "SpeedPasses consumed: ";
        return Result.ok(entry.message() + " " + kind + ids + ".");
    }

    private static List<IndividualTicket> individualTicketsOf(Ticket ticket) {
        if (ticket instanceof GroupTicket group) {
            return group.getTickets();
        }
        return ticket instanceof IndividualTicket single ? List.of(single) : List.of();
    }

    public Result exitAttraction(String attractionId, String ticketId) {
        return attractions.exit(attractionId, tickets.findForAccess(ticketId), ticketId);
    }

    public Result attractionStatus(String attractionId) {
        return attractions.status(attractionId);
    }
}
