package es.upm.etsisi.amusementpark.business.service;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.model.UserValidator;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for groups. Members are looked up through {@link UserManager},
 * so groups always refer to the existing user objects. Every successful change
 * is saved; if saving fails the previous state is restored.
 */
public class GroupManager {

    private final GroupFileRepository repository;
    private final UserManager users;
    private final Map<String, Group> groups = new LinkedHashMap<>();

    public GroupManager(GroupFileRepository repository, UserManager users) throws IOException {
        this.repository = repository;
        this.users = users;
        Map<String, User> byDni = new HashMap<>();
        for (User u : users.getUsers()) {
            byDni.put(u.getDni(), u);
        }
        for (Group g : repository.load(byDni::get)) {
            groups.put(g.getId(), g);
        }
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public Optional<Group> find(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public void create(String id, String name) throws UserException {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new UserException("The group id and name must not be empty");
        }
        if (groups.containsKey(id)) {
            throw new UserException("A group with id " + id + " already exists");
        }
        groups.put(id, new Group(id, name));
        save(() -> groups.remove(id));
    }

    public void addMember(String id, String email) throws UserException {
        Group group = require(id);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UserException("No user with email " + email));
        if (!group.addMember(user)) {
            throw new UserException("User " + email + " is already in group " + id);
        }
        save(() -> group.removeMember(user));
    }

    public void removeMember(String id, String email) throws UserException {
        Group group = require(id);
        Optional<User> user = users.findByEmail(email);
        if (user.isEmpty() || !group.contains(user.get())) {
            throw new UserException("User " + email + " is not in group " + id);
        }
        Group previous = new Group(group);
        group.removeMember(user.get());
        save(() -> groups.put(id, previous));
    }

    /** Deletes the group; its member users are not affected. */
    public void delete(String id) throws UserException {
        require(id);
        Map<String, Group> previous = new LinkedHashMap<>(groups);
        groups.remove(id);
        save(() -> restore(previous));
    }

    /** Removes a (deleted) user from every group that contains it. */
    public void removeUserEverywhere(User user) throws UserException {
        List<Group> affected = new ArrayList<>();
        for (Group g : groups.values()) {
            if (g.contains(user)) {
                affected.add(new Group(g));
                g.removeMember(user);
            }
        }
        if (!affected.isEmpty()) {
            save(() -> affected.forEach(g -> groups.put(g.getId(), g)));
        }
    }

    private void restore(Map<String, Group> previous) {
        groups.clear();
        groups.putAll(previous);
    }

    private Group require(String id) throws UserException {
        Group group = groups.get(id);
        if (group == null) {
            throw new UserException("No group with id " + id);
        }
        return group;
    }

    private void save(Runnable rollback) throws UserException {
        try {
            repository.save(groups.values());
        } catch (IOException e) {
            rollback.run();
            throw new UserException("Could not save groups: " + e.getMessage());
        }
    }
}
