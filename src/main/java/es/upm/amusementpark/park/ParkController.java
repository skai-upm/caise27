package es.upm.amusementpark.park;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.UserStorage;
import es.upm.amusementpark.repository.AttractionRepository;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.AttractionService;
import es.upm.amusementpark.service.GroupService;
import es.upm.amusementpark.service.OperationException;
import es.upm.amusementpark.service.TicketService;
import es.upm.amusementpark.service.UserService;

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

    private void reset(UserRepository repository, UserStorage storage) {
        this.users = new UserService(repository, storage);
        this.tickets = new TicketService(new TicketRepository(), users, this::getCurrentDate, this::getBasePrice);
        this.groups = new GroupService(new GroupRepository(), users, tickets::hasActiveGroupTicket,
                this::getCurrentDate);
        this.attractions = new AttractionService(new AttractionRepository());
        this.currentDate = LocalDate.now();
        this.basePrice = INITIAL_BASE_PRICE;
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
    public void deleteUser(String email, String password) throws OperationException {
        String dni = users.authenticate(email, password).getDni();
        if (tickets.hasActiveTicket(dni)) {
            throw new OperationException("User has active tickets");
        }
        if (groups.findAll().stream().anyMatch(g -> g.hasMember(dni) && groups.isLocked(g))) {
            throw new OperationException("User belongs to a group locked by an active group ticket");
        }
        users.deleteUser(email, password);
        groups.removeUserFromAllGroups(dni);
    }

    /**
     * Buys a group ticket for the members the group has right now.
     */
    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws OperationException {
        Group group = groups.findById(groupId).orElseThrow(() -> new OperationException("Group not found"));
        List<User> members = new ArrayList<>();
        for (String dni : group.getMemberDnis()) {
            members.add(users.findByDni(dni).orElseThrow());
        }
        return tickets.createGroupTicket(id, group, members, date);
    }

    /**
     * Deletes a group; its historical group tickets are kept without the group reference.
     */
    public void deleteGroup(String groupId) throws OperationException {
        Group group = groups.findById(groupId).orElseThrow(() -> new OperationException("Group not found"));
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
