package es.upm.parque.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.security.PasswordEncoder;
import es.upm.parque.business.validation.UserValidator;
import es.upm.parque.persistence.UserRepository;

/**
 * Manages users (Singleton): validation, uniqueness, credentials and deletion rules. Passwords are only kept as
 * hashes. Every operation returns a message starting with "OK" or "ERROR".
 */
public final class UserController {

    private static final String WEAK_PASSWORD =
            "ERROR Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol";
    private static final String INVALID_CARD = "ERROR Invalid card number: it must have exactly 16 digits";

    private static UserController instance;

    private final List<User> users = new ArrayList<>();
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private ParkController park;
    private TicketController tickets;
    private GroupController groups;

    private UserController() {
    }

    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public void configure(UserRepository repository, PasswordEncoder passwordEncoder, ParkController park,
            TicketController tickets, GroupController groups) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.park = park;
        this.tickets = tickets;
        this.groups = groups;
    }

    /**
     * Loads the stored users, replacing any plaintext password left by earlier versions with its hash.
     */
    public void load() throws IOException {
        users.clear();
        users.addAll(repository.load());
        boolean migrated = false;
        for (User user : users) {
            if (!passwordEncoder.isHash(user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.hash(user.getPasswordHash()));
                migrated = true;
            }
        }
        if (migrated) {
            repository.saveAll(users);
        }
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Returns the user with that email if the password matches its stored hash, or {@code null} otherwise.
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public String createUser(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        if (!UserValidator.isNotBlank(name) || !UserValidator.isNotBlank(surname)
                || !UserValidator.isNotBlank(phone)) {
            return "ERROR Name, surname and telephone must not be empty";
        }
        if (!UserValidator.isValidDni(dni)) {
            return "ERROR Invalid DNI: " + dni;
        }
        if (!UserValidator.isValidEmail(email)) {
            return "ERROR Invalid email: " + email;
        }
        if (!UserValidator.isValidPassword(password)) {
            return WEAK_PASSWORD;
        }
        if (!UserValidator.isValidCard(card)) {
            return INVALID_CARD;
        }
        if (findByDni(dni) != null) {
            return "ERROR A user with DNI " + dni + " already exists";
        }
        if (findByEmail(email) != null) {
            return "ERROR A user with email " + email + " already exists";
        }
        User user = new User(name, surname, dni, email, passwordEncoder.hash(password), phone, card);
        users.add(user);
        if (!persist()) {
            users.remove(user);
            return "ERROR Could not save users";
        }
        return "OK USER CREATED " + email;
    }

    public String updateUser(String email, String password, UserField field, String value) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR Unknown email or wrong password";
        }
        String error = validateNewValue(user, field, value);
        if (error != null) {
            return error;
        }
        String previous = get(user, field);
        set(user, field, field == UserField.PASSWORD ? passwordEncoder.hash(value) : value);
        if (!persist()) {
            set(user, field, previous);
            return "ERROR Could not save users";
        }
        return "OK USER UPDATED " + field;
    }

    /**
     * Deletes a user that has no ticket dated on or after the system date, together with its past tickets and
     * its group memberships.
     */
    public String deleteUser(String email, String password) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR Unknown email or wrong password";
        }
        if (tickets.hasCurrentOrFutureTicket(user, park.getCurrentDate())) {
            return "ERROR " + user.getEmail() + " has tickets dated on or after " + park.getCurrentDate()
                    + " and cannot be deleted";
        }
        if (!tickets.removeTicketsOf(user) || !groups.removeUserEverywhere(user) || !remove(user)) {
            return "ERROR Could not save changes";
        }
        return "OK USER DELETED " + user.getEmail();
    }

    private boolean remove(User user) {
        int index = users.indexOf(user);
        users.remove(index);
        if (!persist()) {
            users.add(index, user);
            return false;
        }
        return true;
    }

    private String validateNewValue(User user, UserField field, String value) {
        if (!UserValidator.isNotBlank(value)) {
            return "ERROR The new value must not be empty";
        }
        switch (field) {
            case EMAIL:
                if (!UserValidator.isValidEmail(value)) {
                    return "ERROR Invalid email: " + value;
                }
                User owner = findByEmail(value);
                if (owner != null && owner != user) {
                    return "ERROR A user with email " + value + " already exists";
                }
                return null;
            case PASSWORD:
                return UserValidator.isValidPassword(value) ? null : WEAK_PASSWORD;
            case CARD:
                return UserValidator.isValidCard(value) ? null : INVALID_CARD;
            default:
                return null;
        }
    }

    private static String get(User user, UserField field) {
        switch (field) {
            case NAME:
                return user.getName();
            case SURNAME:
                return user.getSurname();
            case EMAIL:
                return user.getEmail();
            case PASSWORD:
                return user.getPasswordHash();
            case PHONE:
                return user.getPhone();
            default:
                return user.getCard();
        }
    }

    private static void set(User user, UserField field, String value) {
        switch (field) {
            case NAME:
                user.setName(value);
                break;
            case SURNAME:
                user.setSurname(value);
                break;
            case EMAIL:
                user.setEmail(value);
                break;
            case PASSWORD:
                user.setPasswordHash(value);
                break;
            case PHONE:
                user.setPhone(value);
                break;
            default:
                user.setCard(value);
                break;
        }
    }

    private boolean persist() {
        try {
            repository.saveAll(users);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
