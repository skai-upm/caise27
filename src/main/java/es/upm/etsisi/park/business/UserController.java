package es.upm.etsisi.park.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User registration, deletion and modification. Passwords are validated in clear and stored hashed.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();
    private static final String INVALID_EMAIL = "Invalid email";
    private static final String INVALID_PASSWORD = "Invalid password";
    private static final String INVALID_CARD = "Invalid card number";
    private static final String EMAIL_TAKEN = "Email already registered";

    private Park park;
    private PasswordHasher hasher;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park, PasswordHasher hasher) {
        this.park = park;
        this.hasher = hasher;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        Park.requireNotBlank(firstName, surnames, dni, email, password, phone, card);
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException(INVALID_EMAIL);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException(INVALID_PASSWORD);
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserException(INVALID_CARD);
        }
        Map<String, User> users = park.users();
        for (User u : users.values()) {
            if (u.getDni().equals(dni)) {
                throw new UserException("DNI already registered");
            }
        }
        if (users.containsKey(email)) {
            throw new UserException(EMAIL_TAKEN);
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        users.put(email, user);
        park.commit(() -> users.remove(email));
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        if (park.hasActiveTicket(user)) {
            throw new UserException("User has active tickets");
        }
        List<Group> memberships = new ArrayList<>();
        for (Group g : park.groups().values()) {
            if (g.contains(user)) {
                if (park.isGroupLocked(g)) {
                    throw new UserException("User belongs to locked group " + g.getId());
                }
                memberships.add(g);
            }
        }
        Map<String, User> users = park.users();
        Map<String, User> before = new LinkedHashMap<>(users);
        Map<Group, Integer> positions = new LinkedHashMap<>();
        for (Group g : memberships) {
            positions.put(g, g.remove(user));
        }
        users.remove(user.getEmail());
        park.commit(() -> {
            Park.restore(users, before);
            positions.forEach((g, index) -> g.add(index, user));
        });
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("Field cannot be modified: " + fieldName);
        }
        Park.requireNotBlank(newValue);
        validateNewValue(user, field, newValue);

        Map<String, User> users = park.users();
        Map<String, User> before = new LinkedHashMap<>(users);
        String stored = field == UserField.PASSWORD ? hasher.hash(newValue) : newValue;
        String previous = apply(user, field, stored);
        if (field == UserField.EMAIL) {
            users.remove(email);
            users.put(newValue, user);
        }
        park.commit(() -> {
            apply(user, field, previous);
            Park.restore(users, before);
        });
    }

    private void validateNewValue(User user, UserField field, String value) {
        switch (field) {
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserException(INVALID_EMAIL);
                }
                User other = park.users().get(value);
                if (other != null && other != user) {
                    throw new UserException(EMAIL_TAKEN);
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserException(INVALID_PASSWORD);
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new UserException(INVALID_CARD);
                }
            }
            default -> {
                // NAME, SURNAME and PHONE have no format restriction beyond being non-blank.
            }
        }
    }

    /** Sets the field and returns its previous value. */
    private static String apply(User user, UserField field, String value) {
        String previous;
        switch (field) {
            case NAME -> {
                previous = user.getFirstName();
                user.setFirstName(value);
            }
            case SURNAME -> {
                previous = user.getSurnames();
                user.setSurnames(value);
            }
            case EMAIL -> {
                previous = user.getEmail();
                user.setEmail(value);
            }
            case PASSWORD -> {
                previous = user.getPassword();
                user.setPassword(value);
            }
            case PHONE -> {
                previous = user.getPhone();
                user.setPhone(value);
            }
            case CARD -> {
                previous = user.getCard();
                user.setCard(value);
            }
            default -> throw new IllegalStateException("Unhandled field " + field);
        }
        return previous;
    }

    /** Checks the password against the stored BCrypt hash. */
    public boolean checkPassword(User user, String candidate) {
        return hasher.matches(candidate, user.getPassword());
    }

    private User authenticate(String email, String password) {
        User user = park.users().get(email);
        if (user == null) {
            throw new UserException("User not found");
        }
        if (!checkPassword(user, password)) {
            throw new UserException("Incorrect password");
        }
        return user;
    }
}
