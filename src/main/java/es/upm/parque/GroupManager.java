package es.upm.parque;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business rules for groups of registered users. Every successful change is persisted.
 */
public class GroupManager {

    static final String GROUP_CREATED = "OK GROUP CREATED";
    static final String MEMBER_ADDED = "OK USER ADDED TO GROUP";
    static final String MEMBER_REMOVED = "OK USER REMOVED FROM GROUP";
    static final String GROUP_DELETED = "OK GROUP DELETED";
    static final String DUPLICATE_GROUP = "ERROR DUPLICATE GROUP";
    static final String GROUP_NOT_FOUND = "ERROR GROUP NOT FOUND";
    static final String USER_NOT_FOUND = "ERROR USER NOT FOUND";
    static final String ALREADY_MEMBER = "ERROR USER ALREADY IN GROUP";
    static final String NOT_MEMBER = "ERROR USER NOT IN GROUP";

    private final GroupRepository repository;
    private final UserManager users;
    private final Map<String, Group> groupsById = new LinkedHashMap<>();

    public GroupManager(GroupRepository repository, UserManager users) throws IOException {
        this.repository = repository;
        this.users = users;
        for (Group group : repository.load(users::findByDni)) {
            groupsById.put(group.getId(), group);
        }
    }

    public String create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return UserManager.EMPTY_FIELD;
        }
        if (groupsById.containsKey(id)) {
            return DUPLICATE_GROUP;
        }
        groupsById.put(id, new Group(id, name));
        return persist(GROUP_CREATED);
    }

    public String addMember(String id, String email) {
        Group group = groupsById.get(id);
        if (group == null) {
            return GROUP_NOT_FOUND;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return USER_NOT_FOUND;
        }
        if (!group.addMember(user)) {
            return ALREADY_MEMBER;
        }
        return persist(MEMBER_ADDED);
    }

    public String removeMember(String id, String email) {
        Group group = groupsById.get(id);
        if (group == null) {
            return GROUP_NOT_FOUND;
        }
        User user = users.findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            return NOT_MEMBER;
        }
        return persist(MEMBER_REMOVED);
    }

    public String delete(String id) {
        if (groupsById.remove(id) == null) {
            return GROUP_NOT_FOUND;
        }
        return persist(GROUP_DELETED);
    }

    /**
     * Removes a deleted user from every group that contains it.
     */
    public void removeUserFromAll(User user) {
        boolean changed = false;
        for (Group group : groupsById.values()) {
            changed |= group.removeMember(user);
        }
        if (changed) {
            persist(MEMBER_REMOVED);
        }
    }

    public Group find(String id) {
        return groupsById.get(id);
    }

    private String persist(String success) {
        try {
            repository.save(groupsById.values());
            return success;
        } catch (IOException e) {
            return UserManager.STORAGE_FAILURE;
        }
    }
}
