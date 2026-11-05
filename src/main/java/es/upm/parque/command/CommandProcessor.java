package es.upm.parque.command;

import es.upm.parque.model.User;
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
            return "ERROR: unknown command '" + command + "'";
        } catch (RuntimeException e) {
            return "ERROR: the command could not be processed";
        }
    }

    private String createUser(String[] e) {
        if (e.length != USER_CREATE_ELEMENTS) {
            return "ERROR: USER CREATE expects 7 fields: "
                    + "USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card";
        }
        String firstName = e[1];
        String surnames = e[2];
        String dni = e[3];
        String email = e[4];
        String password = e[5];
        String phone = e[6];
        String card = e[7];

        if (!UserValidator.isValidDni(dni)) {
            return "ERROR: invalid DNI '" + dni + "'";
        }
        if (!UserValidator.isValidEmail(email)) {
            return "ERROR: invalid email '" + email + "'";
        }
        if (!UserValidator.isValidPassword(password)) {
            return "ERROR: invalid password (requires uppercase, lowercase, digit and symbol)";
        }
        if (!UserValidator.isValidCardNumber(card)) {
            return "ERROR: invalid card number (requires exactly 16 digits)";
        }
        if (users.existsByDni(dni)) {
            return "ERROR: a user with DNI " + dni + " already exists";
        }
        if (users.existsByEmail(email)) {
            return "ERROR: a user with email " + email + " already exists";
        }
        users.add(new User(firstName, surnames, dni, email, password, phone, card));
        return "OK: user " + dni + " created";
    }
}
