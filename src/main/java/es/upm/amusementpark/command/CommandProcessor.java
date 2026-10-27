package es.upm.amusementpark.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.UserValidator;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.persistence.PersistenceException;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Interprets text commands whose elements are separated by ';' and executes them.
 * The same instance serves interactive and file input.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join("\n",
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>   (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION LIST;<attraction>",
            "HELP",
            "EXIT");

    private final ParkController park;
    private final GroupCommands groups;
    private final TicketCommands tickets;
    private final GroupTicketCommands groupTickets;
    private final AttractionCommands attractions;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
        this.groups = new GroupCommands(park);
        this.tickets = new TicketCommands(park);
        this.groupTickets = new GroupTicketCommands(park);
        this.attractions = new AttractionCommands(park);
    }

    private UserRepository users() {
        return park.getUsers();
    }

    /**
     * Executes one command line and returns the response to show, or null when there
     * is nothing to show (blank line).
     */
    public String process(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
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
                case "SYSTEM DATE":
                    return systemDate(parts);
                case "SYSTEM PRICE":
                    return systemPrice(parts);
                case "GROUP CREATE":
                    return groups.create(parts);
                case "GROUP ADD":
                    return groups.add(parts);
                case "GROUP REMOVE":
                    return groups.remove(parts);
                case "GROUP DELETE":
                    return groups.delete(parts);
                case "TICKET CREATE":
                    return tickets.create(parts);
                case "TICKET DELETE":
                    return tickets.delete(parts);
                case "GROUPTICKET CREATE":
                    return groupTickets.create(parts);
                case "GROUPTICKET DELETE":
                    return groupTickets.delete(parts);
                case "ATTRACTION CREATE":
                    return attractions.create(parts);
                case "ATTRACTION ENTER":
                    return attractions.enter(parts);
                case "ATTRACTION LIST":
                    return attractions.list(parts);
                case "HELP":
                    return HELP_TEXT;
                case "EXIT":
                    exitRequested = true;
                    return null;
                default:
                    return "ERROR: Unknown command";
            }
        } catch (PersistenceException e) {
            return "ERROR: Could not save users";
        } catch (RuntimeException e) {
            return "ERROR: Command could not be processed";
        }
    }

    /** True once EXIT has been processed; no further command must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
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
        if (users().existsDni(dni)) {
            return "ERROR: DNI already registered";
        }
        if (users().existsEmail(email)) {
            return "ERROR: Email already registered";
        }
        users().add(new User(parts[1], parts[2], dni, email, parts[5], parts[6], parts[7]));
        return "USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3 || hasEmpty(parts)) {
            return "ERROR: Invalid format. Use USER DELETE;Email;Password";
        }
        User user = users().findByEmail(parts[1]);
        if (user == null) {
            return "ERROR: User not found";
        }
        if (!user.getPassword().equals(parts[2])) {
            return "ERROR: Incorrect password";
        }
        if (park.hasActiveTicket(user)) {
            return "ERROR: User has active tickets";
        }
        if (park.isInLockedGroup(user)) {
            return "ERROR: User belongs to a group with active group tickets";
        }
        park.removeFromAllGroups(user);
        park.removeFromAllAttractions(user);
        users().remove(user);
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
        User user = users().findByEmail(parts[1]);
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
                User owner = users().findByEmail(value);
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
        users().update(user);
        return "USER UPDATED";
    }

    private String systemDate(String[] parts) {
        LocalDate date = parts.length == 2 ? Dates.parse(parts[1]) : null;
        if (date == null) {
            return "ERROR: Invalid date. Use SYSTEM DATE;<yyyy-MM-dd>";
        }
        park.setCurrentDate(date);
        return "SYSTEM DATE UPDATED";
    }

    private String systemPrice(String[] parts) {
        BigDecimal price = parts.length == 2 ? parseAmount(parts[1]) : null;
        if (price == null || price.signum() <= 0) {
            return "ERROR: Invalid price. Use a positive amount such as SYSTEM PRICE;45.50";
        }
        park.setCurrentPrice(price);
        return "SYSTEM PRICE UPDATED";
    }

    /** Parses digits with an optional '.' and at most two decimals; null if not a monetary amount. */
    static BigDecimal parseAmount(String text) {
        int dot = text.indexOf('.');
        String integer = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integer.isEmpty() || !digits(integer) || !digits(decimals) || decimals.length() > 2
                || (dot >= 0 && decimals.isEmpty())) {
            return null;
        }
        return new BigDecimal(text).setScale(2);
    }

    private static boolean digits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    static boolean hasEmpty(String[] parts) {
        for (String part : parts) {
            if (part.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
