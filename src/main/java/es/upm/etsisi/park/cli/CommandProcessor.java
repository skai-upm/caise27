package es.upm.etsisi.park.cli;

import java.io.PrintStream;

import es.upm.etsisi.park.user.UserException;
import es.upm.etsisi.park.user.UserField;
import es.upm.etsisi.park.user.UserService;

/**
 * Interprets one text command per line. Elements are separated by ';'; the
 * first element is the command keyword and the rest are its arguments.
 * The same processor is used for interactive and file input.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    static final String USER_CREATE = "USER CREATE";
    static final String USER_DELETE = "USER DELETE";
    static final String USER_UPDATE = "USER UPDATE";
    static final String HELP = "HELP";
    static final String EXIT = "EXIT";

    private static final String CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    private static final String DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    private static final String UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    private final UserService users;
    private final PrintStream out;

    public CommandProcessor(UserService users, PrintStream out) {
        this.users = users;
        this.out = out;
    }

    /**
     * Processes a single command line.
     *
     * @return false if the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return true;
        }
        String[] parts = line.split(SEPARATOR, -1);
        String keyword = normalizeKeyword(parts[0]);
        String[] args = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            args[i - 1] = parts[i].trim();
        }
        try {
            switch (keyword) {
                case USER_CREATE -> createUser(args);
                case USER_DELETE -> deleteUser(args);
                case USER_UPDATE -> updateUser(args);
                case HELP -> {
                    requireArgs(args, 0, HELP);
                    printHelp();
                }
                case EXIT -> {
                    requireArgs(args, 0, EXIT);
                    out.println("BYE");
                    return false;
                }
                default -> error("Unknown command '" + parts[0].trim() + "'. Type HELP to list the commands");
            }
        } catch (UserException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Could not process command: " + e.getMessage());
        }
        return true;
    }

    private void createUser(String[] args) throws UserException {
        requireArgs(args, 7, CREATE_SYNTAX);
        users.create(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        out.println("USER CREATED");
    }

    private void deleteUser(String[] args) throws UserException {
        requireArgs(args, 2, DELETE_SYNTAX);
        users.delete(args[0], args[1]);
        out.println("USER DELETED");
    }

    private void updateUser(String[] args) throws UserException {
        requireArgs(args, 4, UPDATE_SYNTAX);
        UserField field = UserField.fromName(args[2]);
        if (field == null) {
            throw new UserException("Field '" + args[2] + "' cannot be modified. Modifiable fields: "
                    + UserField.names());
        }
        users.update(args[0], args[1], field, args[3]);
        out.println("USER UPDATED");
    }

    private void printHelp() {
        out.println("Available commands:");
        out.println("  " + CREATE_SYNTAX);
        out.println("  " + DELETE_SYNTAX);
        out.println("  " + UPDATE_SYNTAX);
        out.println("      <field> is one of: " + UserField.names());
        out.println("  " + HELP);
        out.println("  " + EXIT);
    }

    private void error(String message) {
        out.println("ERROR: " + message);
    }

    private static void requireArgs(String[] args, int expected, String syntax) throws UserException {
        if (args.length != expected) {
            throw new UserException("Wrong number of arguments (" + args.length + ", expected " + expected
                    + "). Usage: " + syntax);
        }
    }

    /** Trims, collapses inner spaces and upper-cases the keyword so "user  create" matches "USER CREATE". */
    static String normalizeKeyword(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String token : raw.trim().split(" ")) {
            if (!token.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(token);
            }
        }
        return sb.toString().toUpperCase();
    }
}
