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
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.file.AttractionFileStorage;
import es.upm.amusementpark.persistence.file.DataLocation;
import es.upm.amusementpark.persistence.file.GroupFileStorage;
import es.upm.amusementpark.persistence.file.TicketFileStorage;
import es.upm.amusementpark.persistence.file.UserFileStorage;
import es.upm.amusementpark.persistence.file.UserStorage;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
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
    private GroupRepository groupRepository;
    private TicketRepository ticketRepository;
    private AttractionRepository attractionRepository;
    private ParkFiles files;
    private DataLocation temporary;
    private LocalDate currentDate;
    private BigDecimal basePrice;

    private ParkController() {
        reset(new UserRepository(), null);
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Prepares the park state for a new execution: loads the stored users and sets the date to
     * today and the base price to its initial value.
     *
     * @return warnings produced while loading stored data
     */
    public List<String> initialize(UserStorage storage) throws IOException {
        List<String> warnings = new ArrayList<>();
        UserRepository repository = new UserRepository();
        for (User user : storage.load(warnings)) {
            repository.save(user);
        }
        reset(repository, storage);
        return warnings;
    }

    /**
     * Prepares the park state for a new execution from the data files of the given location:
     * users, groups, tickets and (empty) attractions with their relationships.
     *
     * @return warnings produced while loading stored data
     */
    public List<String> initialize(DataLocation location) throws IOException {
        List<String> warnings = initialize(new UserFileStorage(location.users()));
        ParkFiles loaded = new ParkFiles(new GroupFileStorage(location.groups()),
                new TicketFileStorage(location.tickets()), new AttractionFileStorage(location.attractions()));
        loaded.groups().load(users::findByDni, warnings).forEach(groupRepository::save);
        loaded.attractions().load(warnings).forEach(attractionRepository::save);
        loaded.tickets().load(users::findByDni, groupRepository::findById, warnings).forEach(ticketRepository::save);
        this.files = loaded;
        return warnings;
    }

    /**
     * Writes groups, tickets and attractions to their files, if this execution uses files.
     */
    public void saveState() throws DataAccessException {
        if (files == null) {
            return;
        }
        try {
            files.groups().save(groupRepository.findAll());
            files.tickets().save(ticketRepository.findAll());
            files.attractions().save(attractionRepository.findAll());
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

    private void reset(UserRepository repository, UserStorage storage) {
        this.groupRepository = new GroupRepository();
        this.ticketRepository = new TicketRepository();
        this.attractionRepository = new AttractionRepository();
        this.files = null;
        this.users = new UserService(repository, storage);
        this.tickets = new TicketService(ticketRepository, users, this::getCurrentDate, this::getBasePrice);
        this.groups = new GroupService(groupRepository, users, tickets::hasActiveGroupTicket, this::getCurrentDate);
        this.attractions = new AttractionService(attractionRepository);
        this.currentDate = LocalDate.now();
        this.basePrice = INITIAL_BASE_PRICE;
    }

    private record ParkFiles(GroupFileStorage groups, TicketFileStorage tickets, AttractionFileStorage attractions) {
    }

    public UserService users() {
        return users;
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
        attractions.findById(attractionId).orElseThrow(() -> new NotFoundException("Attraction not found"));
        Ticket ticket = tickets.findAnyById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        return attractions.enter(attractionId, ticket, currentDate);
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
