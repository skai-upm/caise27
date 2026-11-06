package es.upm.park.controller;

import es.upm.park.model.Group;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Central park management. Exactly one instance exists per execution; it keeps
 * the overall park state: current system date, base ticket price and groups.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_BASE_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal basePrice;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private Predicate<String> userExists;
    private Function<String, Collection<LocalDate>> groupTicketDates;

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the startup state. Intended for tests. */
    void reset() {
        currentDate = LocalDate.now();
        basePrice = INITIAL_BASE_PRICE;
        groups.clear();
        userExists = email -> false;
        groupTicketDates = groupId -> List.of();
    }

    // ---- system settings -------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        if (date == null) {
            throw new ParkException("The system date is required");
        }
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ParkException("The price must be a positive amount");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new ParkException("The price must have at most two decimals");
        }
        basePrice = price.setScale(2);
    }

    // ---- integration points with users and group tickets -------------------

    /** Sets how the existence of a registered user is checked (by email). */
    public void setUserExistsLookup(Predicate<String> lookup) {
        this.userExists = lookup;
    }

    /** Sets how the dates of the group tickets associated with a group are obtained. */
    public void setGroupTicketDatesLookup(Function<String, Collection<LocalDate>> lookup) {
        this.groupTicketDates = lookup;
    }

    // ---- groups -----------------------------------------------------------

    public Optional<Group> findGroup(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    /** A group is locked while it has a group ticket dated today or later (derived, never stored). */
    public boolean isGroupLocked(String id) {
        return groupTicketDates.apply(id).stream().anyMatch(date -> !date.isBefore(currentDate));
    }

    public Group createGroup(String id, String name) {
        requireText(id, "The group identifier is required");
        requireText(name, "The group name is required");
        if (groups.containsKey(id)) {
            throw new ParkException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        return group;
    }

    public void addUserToGroup(String id, String email) {
        Group group = requireModifiableGroup(id);
        if (!userExists.test(email)) {
            throw new ParkException("No registered user with email " + email);
        }
        if (!group.addMember(email)) {
            throw new ParkException("User " + email + " already belongs to group " + id);
        }
    }

    public void removeUserFromGroup(String id, String email) {
        Group group = requireModifiableGroup(id);
        if (!group.removeMember(email)) {
            throw new ParkException("User " + email + " does not belong to group " + id);
        }
    }

    public void deleteGroup(String id) {
        requireGroup(id);
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has a group ticket for today or a later date and cannot be deleted");
        }
        groups.remove(id);
    }

    private Group requireGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new ParkException("No group with identifier " + id);
        }
        return group;
    }

    private Group requireModifiableGroup(String id) {
        Group group = requireGroup(id);
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ParkException(message);
        }
    }
}
