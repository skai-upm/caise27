package es.upm.amusementpark.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central, single-instance manager of the overall park state during one execution.
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private UserDirectory userDirectory = UserDirectory.EMPTY;
    private GroupTicketDates groupTicketDates = GroupTicketDates.NONE;

    private ParkController() {
        reset();
    }

    /**
     * Returns the only central park management instance.
     *
     * @return the singleton instance
     */
    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the start-of-execution state: today's local date, the initial base price and no groups. */
    public synchronized void reset() {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
        groups.clear();
        userDirectory = UserDirectory.EMPTY;
        groupTicketDates = GroupTicketDates.NONE;
    }

    public synchronized void setUserDirectory(UserDirectory directory) {
        userDirectory = Objects.requireNonNull(directory, "directory");
    }

    public synchronized void setGroupTicketDates(GroupTicketDates provider) {
        groupTicketDates = Objects.requireNonNull(provider, "provider");
    }

    public synchronized LocalDate getCurrentDate() {
        return currentDate;
    }

    public synchronized void setCurrentDate(LocalDate date) {
        currentDate = Objects.requireNonNull(date, "date");
    }

    public synchronized BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the base ticket price for tickets created from now on.
     *
     * @param price strictly positive amount
     */
    public synchronized void setCurrentPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        currentPrice = price.setScale(2, RoundingMode.UNNECESSARY);
    }

    // ----- groups -----

    /**
     * Creates an empty group.
     *
     * @throws ParkException if a field is empty or the identifier is already used
     */
    public synchronized Group createGroup(String id, String name) {
        if (isBlank(id) || isBlank(name)) {
            throw new ParkException("Group identifier and name are required");
        }
        if (groups.containsKey(id)) {
            throw new ParkException("Group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        return group;
    }

    public synchronized Group findGroup(String id) {
        return groups.get(id);
    }

    public synchronized Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    /**
     * A group is locked while at least one of its group tickets is dated today or later. Never stored.
     */
    public synchronized boolean isGroupLocked(String id) {
        for (LocalDate date : groupTicketDates.datesOf(id)) {
            if (!date.isBefore(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Adds an existing user to an unlocked group. */
    public synchronized void addUserToGroup(String id, String email) {
        Group group = modifiableGroup(id);
        if (!userDirectory.exists(email)) {
            throw new ParkException("User " + email + " does not exist");
        }
        if (!group.addMember(email)) {
            throw new ParkException("User " + email + " already belongs to group " + id);
        }
    }

    /** Removes a member from an unlocked group. */
    public synchronized void removeUserFromGroup(String id, String email) {
        Group group = modifiableGroup(id);
        if (!group.removeMember(email)) {
            throw new ParkException("User " + email + " does not belong to group " + id);
        }
    }

    /** Deletes an unlocked group. */
    public synchronized void deleteGroup(String id) {
        modifiableGroup(id);
        groups.remove(id);
    }

    private Group modifiableGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new ParkException("Group " + id + " does not exist");
        }
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
