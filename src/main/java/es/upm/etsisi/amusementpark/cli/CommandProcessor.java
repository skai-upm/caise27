package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.UserController;

import java.io.PrintStream;

/**
 * Interprets a single command line and executes it. Never throws for bad input.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "  fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD",
            "HELP",
            "EXIT");

    private final UserController userController;
    private final PrintStream out;

    public CommandProcessor(UserController userController, PrintStream out) {
        this.userController = userController;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return false if the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.trim().split(";", -1);
        String command = parts[0].trim();
        try {
            switch (command) {
                case "USER CREATE" -> {
                    requireArgs(parts, 8, "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>");
                    userController.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    out.println("USER CREATED");
                }
                case "USER DELETE" -> {
                    requireArgs(parts, 3, "USER DELETE;<email>;<password>");
                    userController.delete(parts[1], parts[2]);
                    out.println("USER DELETED");
                }
                case "USER UPDATE" -> {
                    requireArgs(parts, 5, "USER UPDATE;<email>;<password>;<field>;<value>");
                    userController.update(parts[1], parts[2], parts[3], parts[4]);
                    out.println("USER UPDATED");
                }
                case "HELP" -> {
                    requireArgs(parts, 1, "HELP");
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireArgs(parts, 1, "EXIT");
                    out.println("BYE");
                    return false;
                }
                default -> out.println("ERROR: Unknown command: " + command + " (type HELP)");
            }
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: Command could not be processed: " + e.getMessage());
        }
        return true;
    }

    private static void requireArgs(String[] parts, int expected, String usage) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Wrong number of arguments. Usage: " + usage);
        }
    }
}
