package es.upm.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Keeps the groups, enforces group-local rules and keeps the repository up to date.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final List<Group> groups;

    public GroupManager(GroupRepository repository, Function<String, User> userByDni) throws IOException {
        this.repository = repository;
        this.groups = new ArrayList<>(repository.load(userByDni));
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group find(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public String create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR Group identifier and name must not be empty";
        }
        if (find(id) != null) {
            return "ERROR Group " + id + " already exists";
        }
        groups.add(new Group(id, name));
        return persist() ? "OK GROUP CREATED " + id : "ERROR Could not save groups";
    }

    public String addMember(Group group, User user) {
        if (!group.addMember(user)) {
            return "ERROR " + user.getEmail() + " is already in group " + group.getId();
        }
        return persist() ? "OK USER ADDED TO GROUP " + group.getId() : "ERROR Could not save groups";
    }

    public String removeMember(Group group, User user) {
        if (!group.removeMember(user)) {
            return "ERROR " + user.getEmail() + " is not in group " + group.getId();
        }
        return persist() ? "OK USER REMOVED FROM GROUP " + group.getId() : "ERROR Could not save groups";
    }

    public boolean delete(Group group) {
        groups.remove(group);
        return persist();
    }

    /**
     * Removes the user from every group it belongs to.
     */
    public boolean removeUserEverywhere(User user) {
        for (Group group : groups) {
            group.removeMember(user);
        }
        return persist();
    }

    private boolean persist() {
        try {
            repository.saveAll(groups);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
