package es.upm.amusementpark;

import java.util.List;

/**
 * User management operations. Every successful change is saved through the repository.
 */
public class UserService {

    private final UserRepository repository;
    private final List<User> users;

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.users = repository.load();
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireNonEmpty(firstName, "first name");
        requireNonEmpty(surnames, "surnames");
        requireNonEmpty(phone, "phone");
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new IllegalArgumentException("DNI already registered");
        }
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("email already registered");
        }
        users.add(new User(firstName, surnames, dni, email, password, phone, card));
        repository.save(users);
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        users.remove(user);
        repository.save(users);
    }

    public void update(String email, String password, String fieldName, String value) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new IllegalArgumentException("field cannot be modified: " + fieldName);
        }
        switch (field) {
            case NAME -> {
                requireNonEmpty(value, "first name");
                user.setFirstName(value);
            }
            case SURNAME -> {
                requireNonEmpty(value, "surnames");
                user.setSurnames(value);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new IllegalArgumentException("invalid email");
                }
                User other = findByEmail(value);
                if (other != null && other != user) {
                    throw new IllegalArgumentException("email already registered");
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new IllegalArgumentException("invalid password");
                }
                user.setPassword(value);
            }
            case PHONE -> {
                requireNonEmpty(value, "phone");
                user.setPhone(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new IllegalArgumentException("invalid card number");
                }
                user.setCard(value);
            }
        }
        repository.save(users);
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

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("incorrect password");
        }
        return user;
    }

    private static void requireNonEmpty(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }
}
