package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.TransactionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages user groups and their lock, which is derived from active group
 * tickets.
 */
public final class GroupController extends AbstractController {

    private static final GroupController INSTANCE = new GroupController();

    private final Map<String, Group> groups = new LinkedHashMap<>();
    private GroupRepository repository;
    private UserController users;
    private TicketController tickets;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and the collaborating controllers. */
    public void configure(GroupRepository repository, TransactionManager transactions, UserController users,
                          TicketController tickets) {
        this.repository = repository;
        setTransactions(transactions);
        this.users = users;
        this.tickets = tickets;
        groups.clear();
    }

    void load() throws IOException {
        groups.clear();
        for (Group group : repository.loadAll(users::findByDni)) {
            groups.put(group.getId(), group);
        }
    }

    void save() throws IOException {
        repository.saveAll(groups.values());
    }

    public Group find(String id) {
        return groups.get(id);
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups.values());
    }

    public Group require(String id) throws NotFoundException {
        Group group = groups.get(id);
        if (group == null) {
            throw new NotFoundException("No group with identifier " + id);
        }
        return group;
    }

    public void create(String id, String name) throws ParkException {
        requireText(id, "Group identifier");
        requireText(name, "Group name");
        if (groups.containsKey(id)) {
            throw new OperationNotAllowedException("Group " + id + " already exists");
        }
        groups.put(id, new Group(id, name));
        persist(this::save, () -> groups.remove(id));
    }

    public void addMember(String groupId, String email) throws ParkException {
        Group group = require(groupId);
        User user = users.require(email);
        requireModifiable(group);
        if (!group.add(user)) {
            throw new OperationNotAllowedException(email + " is already a member of group " + groupId);
        }
        persist(this::save, () -> group.remove(user));
    }

    public void removeMember(String groupId, String email) throws ParkException {
        Group group = require(groupId);
        User user = users.require(email);
        requireModifiable(group);
        int position = group.remove(user);
        if (position < 0) {
            throw new NotFoundException(email + " is not a member of group " + groupId);
        }
        persist(this::save, () -> group.add(position, user));
    }

    /**
     * Deletes an unlocked group. Its historical group tickets are kept but no
     * longer reference it.
     */
    public void delete(String groupId) throws ParkException {
        Group group = require(groupId);
        requireModifiable(group);
        List<GroupTicket> purchases = tickets.groupTicketsOf(group);
        groups.remove(groupId);
        purchases.forEach(GroupTicket::detachGroup);
        persist(() -> {
            save();
            tickets.save();
        }, () -> {
            groups.put(groupId, group);
            purchases.forEach(ticket -> ticket.restoreGroup(group));
        });
    }

    /**
     * A group is locked while it has an active group ticket. The state is
     * derived from the tickets and the current date, never stored.
     */
    public boolean isLocked(Group group) {
        return tickets.hasActiveGroupTicket(group);
    }

    /** A locked group that {@code user} belongs to, or {@code null}. */
    Group lockedGroupOf(User user) {
        for (Group group : groups.values()) {
            if (group.contains(user) && isLocked(group)) {
                return group;
            }
        }
        return null;
    }

    /** Removes {@code user} from every group, returning the positions for {@link #restoreMemberships}. */
    Map<Group, Integer> removeEverywhere(User user) {
        Map<Group, Integer> memberships = new LinkedHashMap<>();
        for (Group group : groups.values()) {
            int position = group.remove(user);
            if (position >= 0) {
                memberships.put(group, position);
            }
        }
        return memberships;
    }

    void restoreMemberships(User user, Map<Group, Integer> memberships) {
        memberships.forEach((group, position) -> group.add(position, user));
    }

    private void requireModifiable(Group group) throws OperationNotAllowedException {
        if (isLocked(group)) {
            throw new OperationNotAllowedException("Group " + group.getId()
                    + " has an active group ticket and cannot be modified");
        }
    }
}
