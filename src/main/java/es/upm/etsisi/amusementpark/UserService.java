package es.upm.etsisi.amusementpark;

import java.util.ArrayList;
import java.util.List;

/**
 * User management rules. Every operation validates first, applies the change to
 * a copy of the user list, saves it, and only then replaces the in-memory list,
 * so a rejected or failed operation never leaves partial state.
 */
public class UserService {

    private final UserRepository repository;
    private List<User> users;

    public UserService(UserRepository repository) throws UserException {
        this.repository = repository;
        this.users = repository.load();
    }

    public List<User> getUsers() {
        List<User> copies = new ArrayList<>();
        for (User user : users) {
            copies.add(new User(user));
        }
        return copies;
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws UserException {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("invalid DNI");
        }
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        if (findByDni(dni) != null) {
            throw new UserException("a user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new UserException("a user with email " + email + " already exists");
        }
        List<User> updated = new ArrayList<>(users);
        updated.add(new User(firstName, surnames, dni, email, password, phone, card));
        commit(updated);
    }

    public void delete(String email, String password) throws UserException {
        User user = authenticate(email, password);
        List<User> updated = new ArrayList<>(users);
        updated.remove(user);
        commit(updated);
    }

    public void update(String email, String password, String fieldName, String newValue) throws UserException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("field " + fieldName + " cannot be modified");
        }
        User changed = new User(user);
        switch (field) {
            case NAME -> changed.setFirstName(newValue);
            case SURNAME -> changed.setSurnames(newValue);
            case EMAIL -> {
                validateEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new UserException("a user with email " + newValue + " already exists");
                }
                changed.setEmail(newValue);
            }
            case PASSWORD -> {
                validatePassword(newValue);
                changed.setPassword(newValue);
            }
            case PHONE -> changed.setPhone(newValue);
            case CARD -> {
                validateCard(newValue);
                changed.setCard(newValue);
            }
        }
        List<User> updated = new ArrayList<>(users);
        updated.set(updated.indexOf(user), changed);
        commit(updated);
    }

    private User authenticate(String email, String password) throws UserException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserException("no user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new UserException("incorrect password");
        }
        return user;
    }

    private void commit(List<User> updated) throws UserException {
        repository.save(updated);
        users = updated;
    }

    private User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    private User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private static void validateEmail(String email) throws UserException {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("invalid email");
        }
    }

    private static void validatePassword(String password) throws UserException {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("invalid password");
        }
    }

    private static void validateCard(String card) throws UserException {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("invalid card number");
        }
    }
}
