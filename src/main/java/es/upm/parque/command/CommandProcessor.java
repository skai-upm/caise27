package es.upm.parque.command;

import es.upm.parque.model.User;
import es.upm.parque.model.UserField;
import es.upm.parque.model.UserValidator;
import es.upm.parque.repository.UserRepository;

/**
 * Interprets text commands whose elements are separated by ';' and executes them.
 *
 * <p>Every command produces a response text; incorrect commands produce an
 * {@code ERROR} response and never terminate the application.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String USER_CREATE = "USER CREATE";
    private static final int USER_CREATE_ELEMENTS = 8;
    private static final String USER_DELETE = "USER DELETE";
    private static final int USER_DELETE_ELEMENTS = 3;
    private static final String USER_UPDATE = "USER UPDATE";
    private static final int USER_UPDATE_ELEMENTS = 5;
    private static final String INVALID_PASSWORD = "invalid password (requires uppercase, lowercase, digit and symbol)";
    private static final String INVALID_CARD = "invalid card number (requires exactly 16 digits)";

    private final UserRepository users;

    public CommandProcessor(UserRepository users) {
        this.users = users;
    }

    /**
     * Processes one command line.
     *
     * @return the response to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        try {
            String[] elements = line.split(SEPARATOR, -1);
            for (int i = 0; i < elements.length; i++) {
                elements[i] = elements[i].trim();
            }
            String command = elements[0];
            if (USER_CREATE.equals(command)) {
                return createUser(elements);
            }
            if (USER_DELETE.equals(command)) {
                return deleteUser(elements);
            }
            if (USER_UPDATE.equals(command)) {
                return updateUser(elements);
            }
            return error("unknown command '" + command + "'");
        } catch (RuntimeException e) {
            return error("the command could not be processed");
        }
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }

    private static String ok(User user, String action) {
        return "OK: user " + user.getDni() + " " + action;
    }

    private static String duplicate(String attribute, String value) {
        return "a user with " + attribute + " " + value + " already exists";
    }

    private String createUser(String[] e) {
        if (e.length != USER_CREATE_ELEMENTS) {
            return error("USER CREATE expects 7 fields: USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card");
        }
        User user = new User(e[1], e[2], e[3], e[4], e[5], e[6], e[7]);
        String problem = checkNewUser(user);
        if (problem != null) {
            return error(problem);
        }
        users.add(user);
        return ok(user, "created");
    }

    /** Returns why the new user cannot be created, or {@code null} if it can. */
    private String checkNewUser(User user) {
        if (!UserValidator.isValidDni(user.getDni())) {
            return "invalid DNI '" + user.getDni() + "'";
        }
        if (!UserValidator.isValidEmail(user.getEmail())) {
            return "invalid email '" + user.getEmail() + "'";
        }
        if (!UserValidator.isValidPassword(user.getPassword())) {
            return INVALID_PASSWORD;
        }
        if (!UserValidator.isValidCardNumber(user.getCardNumber())) {
            return INVALID_CARD;
        }
        if (users.existsByDni(user.getDni())) {
            return duplicate("DNI", user.getDni());
        }
        if (users.existsByEmail(user.getEmail())) {
            return duplicate("email", user.getEmail());
        }
        return null;
    }

    /** Returns why the credentials do not identify {@code user}, or {@code null} if they do. */
    private static String authenticationProblem(User user, String email, String password) {
        if (user == null) {
            return "no user with email " + email;
        }
        return user.getPassword().equals(password) ? null : "incorrect password";
    }

    private String deleteUser(String[] e) {
        if (e.length != USER_DELETE_ELEMENTS) {
            return error("USER DELETE expects 2 fields: USER DELETE;Email;Password");
        }
        User user = users.findByEmail(e[1]);
        String problem = authenticationProblem(user, e[1], e[2]);
        if (problem != null) {
            return error(problem);
        }
        users.remove(user.getDni());
        return ok(user, "deleted");
    }

    private String updateUser(String[] e) {
        if (e.length != USER_UPDATE_ELEMENTS) {
            return error("USER UPDATE expects 4 fields: USER UPDATE;Email;Password;Field;NewValue");
        }
        User user = users.findByEmail(e[1]);
        String problem = authenticationProblem(user, e[1], e[2]);
        if (problem != null) {
            return error(problem);
        }
        UserField field = UserField.fromName(e[3]);
        if (field == null) {
            return error("field '" + e[3] + "' cannot be modified (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        }
        String newValue = e[4];
        problem = checkNewValue(user, field, newValue);
        if (problem != null) {
            return error(problem);
        }
        applyField(user, field, newValue);
        users.update(user);
        return ok(user, "updated");
    }

    /** Returns a description of why the new value is not acceptable, or {@code null} if it is. */
    private String checkNewValue(User user, UserField field, String value) {
        switch (field) {
            case EMAIL:
                if (!UserValidator.isValidEmail(value)) {
                    return "invalid email '" + value + "'";
                }
                User owner = users.findByEmail(value);
                return owner != null && owner != user ? duplicate("email", value) : null;
            case PASSWORD:
                return UserValidator.isValidPassword(value) ? null : INVALID_PASSWORD;
            case CARD:
                return UserValidator.isValidCardNumber(value) ? null : INVALID_CARD;
            default:
                return null;
        }
    }

    private static void applyField(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setTelephone(value);
            case CARD -> user.setCardNumber(value);
        }
    }
}
