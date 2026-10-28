package es.upm.amusementpark.business;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.DataAccessException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.file.DataLocation;
import es.upm.amusementpark.persistence.file.ParkStorage;
import es.upm.amusementpark.persistence.file.UserStorage;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;

/**
 * Single central manager of the park state during an execution: users, current system date and
 * current base ticket price.
 *
 * <p>Only one instance exists; it is obtained with {@link #getInstance()}.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_BASE_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserService users;
    private GroupService groups;
    private TicketService tickets;
    private AttractionService attractions;
    private InvoiceService invoices;
    private PassService passes;
    private ParkDependencies dependencies;
    private DataLocation temporary;
    private LocalDate currentDate;
    private BigDecimal basePrice;

    private ParkController() {
        ParkInitializer.wire(this, ParkInitializer.inMemory(null));
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Receives the controllers and repositories wired by {@link ParkInitializer} and starts a new
     * execution state: date is today and the base price is the initial one.
     */
    void configure(ParkDependencies dependencies, UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions, InvoiceService invoices, PassService passes) {
        this.dependencies = dependencies;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.invoices = invoices;
        this.passes = passes;
        this.currentDate = LocalDate.now();
        this.basePrice = INITIAL_BASE_PRICE;
    }

    /**
     * Prepares a new execution with in-memory repositories and the given user storage only.
     *
     * @return warnings produced while loading stored data
     */
    public List<String> initialize(UserStorage storage) throws IOException {
        return loadInto(ParkInitializer.inMemory(storage));
    }

    /**
     * Prepares a new execution from the data files of the given location: users, groups, tickets
     * and (empty) attractions with their relationships.
     *
     * @return warnings produced while loading stored data
     */
    public List<String> initialize(DataLocation location) throws IOException {
        return loadInto(ParkInitializer.forLocation(location));
    }

    private List<String> loadInto(ParkDependencies deps) throws IOException {
        ParkInitializer.wire(this, deps);
        List<String> warnings = new ArrayList<>();
        if (deps.userStorage() != null) {
            deps.userStorage().load(warnings).forEach(deps.users()::save);
        }
        ParkStorage storage = deps.storage();
        if (storage != null) {
            storage.groups().load(users::findByDni, warnings).forEach(deps.groups()::save);
            storage.attractions().load(warnings).forEach(deps.attractions()::save);
            storage.tickets().load(users::findByDni, deps.groups()::findById, warnings).forEach(deps.tickets()::save);
            storage.passes().load(deps.tickets()::findIndividualById, warnings).forEach(deps.passes()::save);
        }
        return warnings;
    }

    /**
     * Writes groups, tickets and attractions to their files, if this execution uses files.
     */
    public void saveState() throws DataAccessException {
        ParkStorage storage = dependencies.storage();
        if (storage == null) {
            return;
        }
        try {
            storage.groups().save(dependencies.groups().findAll());
            storage.tickets().save(dependencies.tickets().findAll());
            storage.attractions().save(dependencies.attractions().findAll());
            storage.passes().save(dependencies.passes().findAll());
        } catch (IOException e) {
            throw new DataAccessException("Could not save park data: " + e.getMessage(), e);
        }
    }

    /**
     * Starts an execution on the permanent data stored in {@code directory}.
     */
    public List<String> initialize(Path directory) throws DataAccessException {
        return load(new DataLocation(directory));
    }

    /**
     * Starts an execution on a new temporary data directory created under {@code root}; it is
     * removed by {@link #close()}.
     */
    public List<String> initializeTemporary(Path root) throws DataAccessException {
        try {
            temporary = DataLocation.temporary(root);
        } catch (IOException e) {
            throw new DataAccessException("Cannot create temporary directory in '" + root + "'", e);
        }
        return load(temporary);
    }

    /**
     * Ends the execution: deletes the temporary data directory, if one is in use.
     */
    public void close() throws DataAccessException {
        if (temporary != null) {
            DataLocation location = temporary;
            temporary = null;
            try {
                location.delete();
            } catch (IOException e) {
                throw new DataAccessException("Cannot delete temporary directory '" + location.directory() + "'", e);
            }
        }
    }

    private List<String> load(DataLocation location) throws DataAccessException {
        try {
            return initialize(location);
        } catch (IOException e) {
            throw new DataAccessException("Cannot load data from '" + location.directory() + "'", e);
        }
    }

    public UserService users() {
        return users;
    }

    public PassService passes() {
        return passes;
    }

    /**
     * Deletes a future individual ticket together with all its passes.
     */
    public void deleteIndividualTicket(String id) throws ParkException {
        passes.deletePassesOf(tickets.deleteIndividualTicket(id).individualTickets());
    }

    /**
     * Deletes a future group ticket, its composition and all passes of the composition.
     */
    public void deleteGroupTicket(String id) throws ParkException {
        passes.deletePassesOf(tickets.deleteGroupTicket(id).individualTickets());
    }

    public InvoiceService invoices() {
        return invoices;
    }

    public AttractionService attractions() {
        return attractions;
    }

    public TicketService tickets() {
        return tickets;
    }

    public GroupService groups() {
        return groups;
    }

    /**
     * Deletes the user identified by email and password and removes it from every group.
     */
    public void deleteUser(String email, String password) throws ParkException {
        User user = users.authenticate(email, password);
        if (tickets.hasActiveTicket(user.getDni())) {
            throw new OperationNotAllowedException("User has active tickets");
        }
        if (groups.findAll().stream().anyMatch(g -> g.hasMember(user) && groups.isLocked(g))) {
            throw new OperationNotAllowedException("User belongs to a group locked by an active group ticket");
        }
        users.deleteUser(email, password);
        groups.removeUserFromAllGroups(user);
    }

    /**
     * Buys a group ticket for the members the group has right now.
     */
    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws ParkException {
        Group group = groups.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found"));
        return tickets.createGroupTicket(id, group, group.getMembers(), date);
    }

    /**
     * Lets the users of a ticket enter an attraction.
     */
    public List<User> enterAttraction(String attractionId, String ticketId) throws ParkException {
        return enterAttraction(attractionId, ticketId, false).users();
    }

    /**
     * Lets the users of a ticket enter an attraction, optionally using SpeedPasses.
     */
    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) throws ParkException {
        attractions.findById(attractionId).orElseThrow(() -> new NotFoundException("Attraction not found"));
        Ticket ticket = tickets.findAnyById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        return attractions.enter(attractionId, ticket, currentDate, speed);
    }

    /**
     * Deletes a group; its historical group tickets are kept without the group reference.
     */
    public void deleteGroup(String groupId) throws ParkException {
        Group group = groups.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found"));
        groups.deleteGroup(groupId);
        tickets.detachGroup(group);
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}
