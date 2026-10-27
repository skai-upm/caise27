package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves groups by identifier.
 */
public class GroupRepository {

    private final Map<String, Group> groups = new LinkedHashMap<>();

    public void save(Group group) {
        groups.put(group.getIdentifier(), group);
    }

    public Optional<Group> findById(String identifier) {
        return Optional.ofNullable(groups.get(identifier));
    }

    public boolean delete(String identifier) {
        return groups.remove(identifier) != null;
    }

    public List<Group> findAll() {
        return new ArrayList<>(groups.values());
    }

    /** Removes the user from every group it belongs to. */
    public void removeUserFromAll(User user) {
        for (Group group : groups.values()) {
            group.removeMember(user);
        }
    }
}
