package es.upm.etsisi.park.cli;

import es.upm.etsisi.park.service.UserOperationException;
import es.upm.etsisi.park.service.UserService;

import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * Interprets one ';'-separated command line and executes it. Never throws: every
 * failure is reported on the output stream so processing can continue.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String USER_CREATE = "USER CREATE";
    private static final String USER_DELETE = "USER DELETE";
    private static final String USER_UPDATE = "USER UPDATE";
    private static final String HELP = "HELP";
    private static final String EXIT = "EXIT";

    private static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    private static final String USER_DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    private static final String USER_UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    private final UserService userService;
    private final PrintStream out;

    public CommandProcessor(UserService userService, PrintStream out) {
        this.userService = userService;
        this.out = out;
    }

    /**
     * Processes a single command line.
     *
     * @return false when the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = parts[0].toUpperCase();
        try {
            switch (command) {
                case USER_CREATE -> createUser(parts);
                case USER_DELETE -> deleteUser(parts);
                case USER_UPDATE -> updateUser(parts);
                case HELP -> printHelp();
                case EXIT -> {
                    return false;
                }
                default -> error("Unknown command '" + parts[0] + "'. Type HELP to list the available commands");
            }
        } catch (UserOperationException e) {
            error(e.getMessage());
        } catch (UncheckedIOException e) {
            error(e.getMessage());
        }
        return true;
    }

    private void createUser(String[] parts) {
        if (checkArguments(parts, 7, USER_CREATE_SYNTAX)) {
            userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
            out.println("USER CREATED");
        }
    }

    private void deleteUser(String[] parts) {
        if (checkArguments(parts, 2, USER_DELETE_SYNTAX)) {
            userService.delete(parts[1], parts[2]);
            out.println("USER DELETED");
        }
    }

    private void updateUser(String[] parts) {
        if (checkArguments(parts, 4, USER_UPDATE_SYNTAX)) {
            userService.update(parts[1], parts[2], parts[3], parts[4]);
            out.println("USER UPDATED");
        }
    }

    private void printHelp() {
        out.println("Available commands:");
        out.println("  " + USER_CREATE_SYNTAX);
        out.println("  " + USER_DELETE_SYNTAX);
        out.println("  " + USER_UPDATE_SYNTAX);
        out.println("      <field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD");
        out.println("  " + HELP);
        out.println("  " + EXIT);
    }

    private boolean checkArguments(String[] parts, int expected, String syntax) {
        if (parts.length - 1 != expected) {
            error("Wrong number of arguments. Usage: " + syntax);
            return false;
        }
        return true;
    }

    private void error(String message) {
        out.println("ERROR: " + message);
    }
}
