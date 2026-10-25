package es.upm.parque;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Keeps the groups and their membership rules. Rejected operations throw
 * {@link IllegalArgumentException} with the reason.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final Map<String, Group> groups = new LinkedHashMap<>();

    public GroupManager(GroupRepository repository, Function<String, User> userByDni) {
        this.repository = repository;
        for (Group g : repository.load(userByDni)) {
            groups.put(g.getId(), g);
        }
    }

    public List<Group> getGroups() {
        return List.copyOf(groups.values());
    }

    public Group find(String id) {
        return groups.get(id);
    }

    public Group get(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + id);
        }
        return group;
    }

    public Group create(String id, String name) {
        Identifiers.require(id, "group");
        if (!UserValidator.isNotEmpty(name)) {
            throw new IllegalArgumentException("Empty group name");
        }
        if (groups.containsKey(id)) {
            throw new IllegalArgumentException("Group already exists: " + id);
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        save();
        return group;
    }

    public void addMember(Group group, User user) {
        if (group.contains(user)) {
            throw new IllegalArgumentException("User " + user.getEmail() + " is already in group " + group.getId());
        }
        group.addMember(user);
        save();
    }

    public void removeMember(Group group, User user) {
        if (!group.removeMember(user)) {
            throw new IllegalArgumentException("User " + user.getEmail() + " is not in group " + group.getId());
        }
        save();
    }

    public void delete(Group group) {
        groups.remove(group.getId());
        save();
    }

    /**
     * Removes the user from every group it belongs to (used when the user is deleted).
     */
    public void removeUserEverywhere(User user) {
        boolean changed = false;
        for (Group g : groups.values()) {
            changed |= g.removeMember(user);
        }
        if (changed) {
            save();
        }
    }

    private void save() {
        repository.save(groups.values());
    }
}
