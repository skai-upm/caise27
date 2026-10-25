package es.upm.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User operations: creation, deletion and update with validation and uniqueness rules.
 * Every successful change is written through the repository.
 */
public class UserManager {

    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equalsIgnoreCase(dni)) {
                return u;
            }
        }
        return null;
    }

    public String create(String name, String surname, String dni, String email,
                         String password, String phone, String card) throws IOException {
        if (name.isBlank() || surname.isBlank() || phone.isBlank()) {
            return "ERROR name, surname and telephone are required";
        }
        if (!UserValidator.isValidDni(dni)) {
            return "ERROR invalid DNI";
        }
        if (!UserValidator.isValidEmail(email)) {
            return "ERROR invalid email";
        }
        if (!UserValidator.isValidPassword(password)) {
            return "ERROR invalid password";
        }
        if (!UserValidator.isValidCard(card)) {
            return "ERROR invalid card number";
        }
        String normalizedDni = dni.toUpperCase();
        if (findByDni(normalizedDni) != null) {
            return "ERROR a user with that DNI already exists";
        }
        if (findByEmail(email) != null) {
            return "ERROR a user with that email already exists";
        }
        users.add(new User(name, surname, normalizedDni, email, password, phone, card));
        repository.save(users);
        return "OK USER CREATED";
    }

    public String delete(String email, String password) throws IOException {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR invalid email or password";
        }
        users.remove(user);
        repository.save(users);
        return "OK USER DELETED";
    }

    public String update(String email, String password, String fieldName, String value) throws IOException {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR invalid email or password";
        }
        UserField field = UserField.fromString(fieldName);
        if (field == null) {
            return "ERROR field not editable: " + fieldName;
        }
        switch (field) {
            case NAME -> {
                if (value.isBlank()) {
                    return "ERROR name cannot be empty";
                }
                user.setName(value);
            }
            case SURNAME -> {
                if (value.isBlank()) {
                    return "ERROR surname cannot be empty";
                }
                user.setSurname(value);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    return "ERROR invalid email";
                }
                User other = findByEmail(value);
                if (other != null && other != user) {
                    return "ERROR a user with that email already exists";
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    return "ERROR invalid password";
                }
                user.setPassword(value);
            }
            case PHONE -> {
                if (value.isBlank()) {
                    return "ERROR telephone cannot be empty";
                }
                user.setPhone(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    return "ERROR invalid card number";
                }
                user.setCard(value);
            }
        }
        repository.save(users);
        return "OK USER UPDATED";
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }
}
