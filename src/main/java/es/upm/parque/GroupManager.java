package es.upm.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the groups and their memberships, persisting every change.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final List<Group> groups;

    public GroupManager(GroupRepository repository, UserManager userManager) throws IOException {
        this.repository = repository;
        this.groups = new ArrayList<>(repository.load(userManager::findByDni));
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group find(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public String create(String id, String name) throws IOException {
        if (id.isBlank() || name.isBlank()) {
            return "ERROR group id and name are required";
        }
        if (find(id) != null) {
            return "ERROR group " + id + " already exists";
        }
        groups.add(new Group(id, name));
        repository.save(groups);
        return "OK GROUP CREATED";
    }

    public String add(Group group, User user) throws IOException {
        if (group.contains(user)) {
            return "ERROR user " + user.getEmail() + " is already in group " + group.getId();
        }
        group.addUser(user);
        repository.save(groups);
        return "OK USER ADDED TO GROUP";
    }

    public String remove(Group group, User user) throws IOException {
        if (!group.removeUser(user)) {
            return "ERROR user " + user.getEmail() + " is not in group " + group.getId();
        }
        repository.save(groups);
        return "OK USER REMOVED FROM GROUP";
    }

    public String delete(Group group) throws IOException {
        groups.remove(group);
        repository.save(groups);
        return "OK GROUP DELETED";
    }

    /**
     * Removes a user from every group (used when the user is deleted).
     */
    public void removeUserFromAll(User user) throws IOException {
        boolean changed = false;
        for (Group g : groups) {
            changed |= g.removeUser(user);
        }
        if (changed) {
            repository.save(groups);
        }
    }
}
