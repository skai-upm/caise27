package es.upm.parque;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for groups of registered users. Every successful change is persisted.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final UserManager userManager;
    private List<Group> groups;

    public GroupManager(GroupRepository repository, UserManager userManager) {
        this.repository = repository;
        this.userManager = userManager;
        this.groups = load();
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group findById(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null);
    }

    public void create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ParkException("The group identifier and name are required");
        }
        if (findById(id) != null) {
            throw new ParkException("A group with identifier " + id + " already exists");
        }
        groups.add(new Group(id, name));
        persist();
    }

    public void delete(String id) {
        groups.remove(requireGroup(id));
        persist();
    }

    public void addMember(String id, String email) {
        Group group = requireGroup(id);
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new ParkException("No user with email " + email);
        }
        if (group.hasMember(user)) {
            throw new ParkException("User " + email + " is already a member of group " + id);
        }
        group.addMember(user);
        persist();
    }

    public void removeMember(String id, String email) {
        Group group = requireGroup(id);
        User user = userManager.findByEmail(email);
        if (user == null || !group.hasMember(user)) {
            throw new ParkException("User " + email + " is not a member of group " + id);
        }
        group.removeMember(user);
        persist();
    }

    /**
     * Removes a deleted user from every group that contains it.
     */
    public void removeUserFromAllGroups(User user) {
        boolean changed = false;
        for (Group g : groups) {
            changed |= g.removeMember(user);
        }
        if (changed) {
            persist();
        }
    }

    private Group requireGroup(String id) {
        Group group = findById(id);
        if (group == null) {
            throw new ParkException("No group with identifier " + id);
        }
        return group;
    }

    private List<Group> load() {
        Map<String, User> usersByDni = new HashMap<>();
        for (User u : userManager.getUsers()) {
            usersByDni.put(u.getDni(), u);
        }
        return new ArrayList<>(repository.loadAll(usersByDni));
    }

    private void persist() {
        try {
            repository.saveAll(groups);
        } catch (UncheckedIOException e) {
            groups = load();
            throw new ParkException("Could not save groups: " + e.getMessage());
        }
    }
}
