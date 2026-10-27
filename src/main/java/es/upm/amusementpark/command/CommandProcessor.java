package es.upm.amusementpark.command;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.UserValidator;
import es.upm.amusementpark.persistence.PersistenceException;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Interprets text commands whose elements are separated by ';' and executes them.
 * The same instance serves interactive and file input.
 */
public class CommandProcessor {

    private final UserRepository users;

    public CommandProcessor(UserRepository users) {
        this.users = users;
    }

    /**
     * Executes one command line and returns the response to show.
     */
    public String process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            switch (parts[0]) {
                case "USER CREATE":
                    return createUser(parts);
                case "USER DELETE":
                    return deleteUser(parts);
                case "USER UPDATE":
                    return updateUser(parts);
                default:
                    return "ERROR: Unknown command";
            }
        } catch (PersistenceException e) {
            return "ERROR: Could not save users";
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != 8 || hasEmpty(parts)) {
            return "ERROR: Invalid format. Use USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card";
        }
        String dni = parts[3];
        String email = parts[4];
        if (!UserValidator.isValidDni(dni)) {
            return "ERROR: Invalid DNI";
        }
        if (!UserValidator.isValidEmail(email)) {
            return "ERROR: Invalid email";
        }
        if (!UserValidator.isValidPassword(parts[5])) {
            return "ERROR: Invalid password";
        }
        if (!UserValidator.isValidCard(parts[7])) {
            return "ERROR: Invalid card number";
        }
        if (users.existsDni(dni)) {
            return "ERROR: DNI already registered";
        }
        if (users.existsEmail(email)) {
            return "ERROR: Email already registered";
        }
        users.add(new User(parts[1], parts[2], dni, email, parts[5], parts[6], parts[7]));
        return "USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3 || hasEmpty(parts)) {
            return "ERROR: Invalid format. Use USER DELETE;Email;Password";
        }
        User user = users.findByEmail(parts[1]);
        if (user == null) {
            return "ERROR: User not found";
        }
        if (!user.getPassword().equals(parts[2])) {
            return "ERROR: Incorrect password";
        }
        users.remove(user);
        return "USER DELETED";
    }

    private String updateUser(String[] parts) {
        if (parts.length != 5 || hasEmpty(parts)) {
            return "ERROR: Invalid format. Use USER UPDATE;Email;Password;Field;NewValue";
        }
        UserField field = UserField.fromText(parts[3]);
        if (field == null) {
            return "ERROR: Field cannot be modified. Use NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD";
        }
        User user = users.findByEmail(parts[1]);
        if (user == null) {
            return "ERROR: User not found";
        }
        if (!user.getPassword().equals(parts[2])) {
            return "ERROR: Incorrect password";
        }
        String value = parts[4];
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case PHONE -> user.setPhone(value);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    return "ERROR: Invalid email";
                }
                User owner = users.findByEmail(value);
                if (owner != null && owner != user) {
                    return "ERROR: Email already registered";
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    return "ERROR: Invalid password";
                }
                user.setPassword(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    return "ERROR: Invalid card number";
                }
                user.setCardNumber(value);
            }
        }
        users.update(user);
        return "USER UPDATED";
    }

    private static boolean hasEmpty(String[] parts) {
        for (String part : parts) {
            if (part.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
