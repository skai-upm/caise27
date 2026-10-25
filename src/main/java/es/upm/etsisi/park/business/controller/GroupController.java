package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.validation.UserValidator;
import es.upm.etsisi.park.persistence.GroupRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Singleton managing user groups. Lock rules depending on tickets are enforced by {@link ParkController}.
 */
public final class GroupController {

    private static GroupController instance;

    private GroupRepository repository;
    private final Map<String, Group> groups = new LinkedHashMap<>();

    private GroupController() {
    }

    public static synchronized GroupController getInstance() {
        if (instance == null) {
            instance = new GroupController();
        }
        return instance;
    }

    public void initialize(GroupRepository groupRepository, UserController users) {
        this.repository = groupRepository;
        groups.clear();
        for (Group group : groupRepository.loadAll(users::findByDni)) {
            groups.put(group.getId(), group);
        }
    }

    public Group create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ValidationException("Group identifier and name must not be empty");
        }
        if (groups.containsKey(id)) {
            throw new DuplicateException("Group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        persist();
        return group;
    }

    public void addMember(Group group, User user) {
        if (!group.add(user)) {
            throw new DuplicateException("User " + user.getEmail() + " already belongs to group " + group.getId());
        }
        persist();
    }

    public void removeMember(Group group, User user) {
        if (!group.remove(user)) {
            throw new NotFoundException("User " + user.getEmail() + " does not belong to group " + group.getId());
        }
        persist();
    }

    /**
     * Removes the user from every group (used when the user is deleted).
     */
    public void removeEverywhere(User user) {
        for (Group group : groups.values()) {
            group.remove(user);
        }
        persist();
    }

    public void delete(Group group) {
        groups.remove(group.getId());
        persist();
    }

    public Group require(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new NotFoundException("Group " + id + " does not exist");
        }
        return group;
    }

    public Optional<Group> find(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    private void persist() {
        repository.saveAll(groups.values());
    }
}
