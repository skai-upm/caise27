package es.upm.amusementpark.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.GroupRepository;

/**
 * Group management. A group's locked state is never stored: it is derived from its active group
 * tickets and the current system date on every check.
 */
public class GroupService {

    private final GroupRepository repository;
    private final UserService users;
    private final ActiveGroupTicketCheck activeTickets;
    private final Supplier<LocalDate> today;

    public GroupService(GroupRepository repository, UserService users, ActiveGroupTicketCheck activeTickets,
            Supplier<LocalDate> today) {
        this.repository = repository;
        this.users = users;
        this.activeTickets = activeTickets;
        this.today = today;
    }

    public Optional<Group> findById(String id) {
        return repository.findById(id);
    }

    public List<Group> findAll() {
        return repository.findAll();
    }

    public boolean isLocked(Group group) {
        return activeTickets.hasActiveGroupTicket(group.getId(), today.get());
    }

    public Group createGroup(String id, String name) throws OperationException {
        if (id.isEmpty() || name.isEmpty()) {
            throw new OperationException("Group identifier and name are required");
        }
        if (repository.findById(id).isPresent()) {
            throw new OperationException("A group with this identifier already exists");
        }
        Group group = new Group(id, name);
        repository.save(group);
        return group;
    }

    public void addMember(String groupId, String email) throws OperationException {
        Group group = modifiableGroup(groupId);
        User user = users.findByEmail(email).orElseThrow(() -> new OperationException("User not found"));
        if (!group.addMember(user.getDni())) {
            throw new OperationException("User already belongs to this group");
        }
    }

    public void removeMember(String groupId, String email) throws OperationException {
        Group group = modifiableGroup(groupId);
        Optional<User> user = users.findByEmail(email);
        if (user.isEmpty() || !group.removeMember(user.get().getDni())) {
            throw new OperationException("User does not belong to this group");
        }
    }

    public void deleteGroup(String groupId) throws OperationException {
        Group group = existingGroup(groupId);
        if (isLocked(group)) {
            throw new OperationException("Group has an active group ticket");
        }
        repository.delete(groupId);
    }

    /**
     * Removes a deleted user from every group.
     */
    public void removeUserFromAllGroups(String dni) {
        repository.findAll().forEach(g -> g.removeMember(dni));
    }

    private Group existingGroup(String groupId) throws OperationException {
        return repository.findById(groupId).orElseThrow(() -> new OperationException("Group not found"));
    }

    private Group modifiableGroup(String groupId) throws OperationException {
        Group group = existingGroup(groupId);
        if (isLocked(group)) {
            throw new OperationException("Group is locked by an active group ticket");
        }
        return group;
    }
}
