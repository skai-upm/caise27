package es.upm.amusementpark.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;

/**
 * Interprets text commands whose elements are separated by {@code ;} and returns the response to
 * print.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int PRICE_DECIMALS = 2;

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>  (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "HELP",
            "EXIT");

    private final ParkController park;
    private final UserService userService;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
        this.userService = park.users();
    }

    /**
     * Processes one command line.
     *
     * @return the response to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            return switch (parts[0]) {
                case "USER CREATE" -> userCreate(parts);
                case "USER DELETE" -> userDelete(parts);
                case "USER UPDATE" -> userUpdate(parts);
                case "SYSTEM DATE" -> systemDate(parts);
                case "SYSTEM PRICE" -> systemPrice(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> error("Unknown command");
            };
        } catch (UserOperationException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected error processing command");
        }
    }

    /**
     * Tells whether an {@code EXIT} command has been processed; no further commands must run.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String systemDate(String[] parts) throws UserOperationException {
        requireArguments(parts, 1, "SYSTEM DATE");
        try {
            park.setCurrentDate(LocalDate.parse(parts[1], DATE_FORMAT));
        } catch (DateTimeParseException e) {
            throw new UserOperationException("Invalid date, expected yyyy-MM-dd: " + parts[1]);
        }
        return "SYSTEM DATE UPDATED";
    }

    private String systemPrice(String[] parts) throws UserOperationException {
        requireArguments(parts, 1, "SYSTEM PRICE");
        BigDecimal price = parsePrice(parts[1]);
        if (price == null) {
            throw new UserOperationException("Invalid price, expected a positive amount: " + parts[1]);
        }
        park.setBasePrice(price);
        return "SYSTEM PRICE UPDATED";
    }

    /**
     * Parses a positive amount with at most two decimals, or returns {@code null}.
     */
    static BigDecimal parsePrice(String text) {
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (whole.isEmpty() || !digits(whole) || !digits(decimals) || decimals.length() > PRICE_DECIMALS
                || (dot >= 0 && decimals.isEmpty())) {
            return null;
        }
        BigDecimal price = new BigDecimal(text).setScale(PRICE_DECIMALS);
        return price.signum() > 0 ? price : null;
    }

    private static boolean digits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private String help(String[] parts) throws UserOperationException {
        requireArguments(parts, 0, "HELP");
        return HELP_TEXT;
    }

    private String exit(String[] parts) throws UserOperationException {
        requireArguments(parts, 0, "EXIT");
        exitRequested = true;
        return "Goodbye";
    }

    private String userCreate(String[] parts) throws UserOperationException {
        requireArguments(parts, 7, "USER CREATE");
        userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "USER CREATED";
    }

    private String userDelete(String[] parts) throws UserOperationException {
        requireArguments(parts, 2, "USER DELETE");
        userService.deleteUser(parts[1], parts[2]);
        return "USER DELETED";
    }

    private String userUpdate(String[] parts) throws UserOperationException {
        requireArguments(parts, 4, "USER UPDATE");
        UserField field = UserField.fromName(parts[3])
                .orElseThrow(() -> new UserOperationException("Field cannot be modified: " + parts[3]));
        userService.updateUser(parts[1], parts[2], field, parts[4]);
        return "USER UPDATED";
    }

    private static void requireArguments(String[] parts, int expected, String command)
            throws UserOperationException {
        if (parts.length - 1 != expected) {
            throw new UserOperationException("Wrong number of arguments for " + command);
        }
    }

    private static String error(String reason) {
        return "ERROR: " + reason;
    }
}
