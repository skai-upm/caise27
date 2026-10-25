package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of registered users. Members are the registered User objects themselves.
 */
public class Group {

    private final String id;
    private final String name;
    private final List<User> users = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public boolean contains(User user) {
        return users.contains(user);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public boolean removeUser(User user) {
        return users.remove(user);
    }
}
