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

    private User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws UserOperationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserOperationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserOperationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserOperationException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserOperationException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new UserOperationException("a user with this DNI already exists");
        }
        if (findByEmail(email) != null) {
            throw new UserOperationException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    public void delete(String email, String password) throws UserOperationException {
        User user = authenticate(email, password);
        users.remove(user);
        repository.save(users);
    }

    public void update(String email, String password, String fieldName, String newValue)
            throws UserOperationException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserOperationException("field cannot be modified: " + fieldName);
        }
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new UserOperationException("invalid email");
                }
                User other = findByEmail(newValue);
                if (other != null && other != user) {
                    throw new UserOperationException("a user with this email already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new UserOperationException("invalid password");
                }
                user.setPassword(newValue);
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new UserOperationException("invalid card number");
                }
                user.setCard(newValue);
            }
        }
        repository.save(users);
    }

    private User authenticate(String email, String password) throws UserOperationException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserOperationException("user not found");
        }
        if (!user.checkPassword(password)) {
            throw new UserOperationException("incorrect password");
        }
        return user;
    }
}
