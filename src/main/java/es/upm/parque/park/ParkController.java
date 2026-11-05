package es.upm.parque.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import es.upm.parque.model.Group;
import es.upm.parque.repository.GroupRepository;
import es.upm.parque.repository.UserRepository;

/**
 * Central park management: the single object that coordinates the overall park state
 * during an execution (users, current system date and current base ticket price).
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private UserRepository users = new UserRepository();
    private GroupRepository groups = new GroupRepository();
    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal basePrice = INITIAL_PRICE;

    private ParkController() {
    }

    private static final class Holder {
        private static final ParkController INSTANCE = new ParkController();
    }

    /** Returns the only central park management instance. */
    public static ParkController getInstance() {
        return Holder.INSTANCE;
    }

    /** Starts a new execution: uses the given users and resets the date to today and the price to 40.00. */
    public void startExecution(UserRepository users) {
        this.users = users;
        this.groups = new GroupRepository();
        this.currentDate = LocalDate.now(ZoneId.systemDefault());
        this.basePrice = INITIAL_PRICE;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    /**
     * Whether the group has an active group ticket, i.e. an associated group ticket dated on or after
     * the current system date. Derived on every call, never stored. No group tickets exist yet.
     */
    public boolean hasActiveGroupTicket(Group group) {
        Objects.requireNonNull(group, "group");
        return false;
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
