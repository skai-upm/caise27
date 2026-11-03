package es.upm.etsisi.park;

import java.io.PrintStream;

/**
 * Parses and executes one command line. Shared by interactive and file mode.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "HELP",
            "EXIT");

    private final UserService users;
    private final PrintStream out;

    public CommandProcessor(UserService users, PrintStream out) {
        this.users = users;
        this.out = out;
    }

    /**
     * Executes a command line.
     *
     * @return {@code false} when the command asks the application to stop
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String command = parts[0];
        try {
            switch (command) {
                case "USER CREATE" -> {
                    expectParams(parts, 7, "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>");
                    users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    out.println("USER CREATED");
                }
                case "USER DELETE" -> {
                    expectParams(parts, 2, "USER DELETE;<email>;<password>");
                    users.delete(parts[1], parts[2]);
                    out.println("USER DELETED");
                }
                case "USER UPDATE" -> {
                    expectParams(parts, 4, "USER UPDATE;<email>;<password>;<field>;<value>");
                    users.update(parts[1], parts[2], parts[3], parts[4]);
                    out.println("USER UPDATED");
                }
                case "HELP" -> {
                    expectParams(parts, 0, "HELP");
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    expectParams(parts, 0, "EXIT");
                    out.println("BYE");
                    return false;
                }
                default -> out.println("ERROR: unknown command '" + command + "'. Type HELP for the list of commands");
            }
        } catch (ValidationException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private static void expectParams(String[] parts, int expected, String syntax) throws ValidationException {
        if (parts.length - 1 != expected) {
            throw new ValidationException("wrong number of parameters. Usage: " + syntax);
        }
    }
}
