package es.upm.etsisi.amusementpark;

import java.util.ArrayList;
import java.util.List;

/**
 * User operations: creation, deletion and modification, keeping the repository up to date.
 */
public class UserService {

    private final UserRepository repository;
    private final List<User> users;

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws OperationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new OperationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new OperationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new OperationException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new OperationException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new OperationException("a user with this DNI already exists");
        }
        if (findByEmail(email) != null) {
            throw new OperationException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    /**
     * Removes an active user. Callers are responsible for checking that removal is allowed.
     */
    public void remove(User user) {
        users.remove(user);
        repository.save(users);
    }

    public void save() {
        repository.save(users);
    }

    public void update(String email, String password, String fieldName, String newValue)
            throws OperationException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new OperationException("field cannot be modified: " + fieldName);
        }
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new OperationException("invalid email");
                }
                User other = findByEmail(newValue);
                if (other != null && other != user) {
                    throw new OperationException("a user with this email already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new OperationException("invalid password");
                }
                user.setPassword(newValue);
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new OperationException("invalid card number");
                }
                user.setCard(newValue);
            }
        }
        repository.save(users);
    }

    public User authenticate(String email, String password) throws OperationException {
        User user = findByEmail(email);
        if (user == null) {
            throw new OperationException("user not found");
        }
        if (!user.checkPassword(password)) {
            throw new OperationException("incorrect password");
        }
        return user;
    }
}
