package es.upm.amusementpark;

import java.io.PrintStream;

/**
 * Interprets a single ';'-separated command line and prints its result.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "  <field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
            "HELP",
            "EXIT");

    private final UserService userService;
    private final PrintStream out;

    public CommandProcessor(UserService userService, PrintStream out) {
        this.userService = userService;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} if the application must stop (EXIT), {@code true} otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String command = parts[0].trim();
        try {
            switch (command) {
                case "USER CREATE" -> {
                    requireArgs(parts, 7);
                    userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    out.println("USER CREATED");
                }
                case "USER DELETE" -> {
                    requireArgs(parts, 2);
                    userService.delete(parts[1], parts[2]);
                    out.println("USER DELETED");
                }
                case "USER UPDATE" -> {
                    requireArgs(parts, 4);
                    userService.update(parts[1], parts[2], parts[3], parts[4]);
                    out.println("USER UPDATED");
                }
                case "HELP" -> {
                    requireArgs(parts, 0);
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireArgs(parts, 0);
                    out.println("EXIT");
                    return false;
                }
                default -> out.println("ERROR: unknown command: " + command);
            }
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: could not process command: " + e.getMessage());
        }
        return true;
    }

    private static void requireArgs(String[] parts, int expected) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("expected " + expected + " arguments but got "
                    + (parts.length - 1) + " (type HELP for syntax)");
        }
    }
}
