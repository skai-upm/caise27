package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Interprets text commands. The same instance and {@link #run} loop serve interactive and file input.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "    <field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
            "HELP",
            "EXIT");

    private final UserService userService;
    private boolean exitRequested;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input, PrintStream output) throws IOException {
        String line;
        while (!exitRequested && (line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            output.println(process(line));
        }
    }

    /**
     * Executes one command line and returns the message to show. Never throws for bad input.
     */
    public String process(String line) {
        String[] parts = line.strip().split(";", -1);
        String command = parts[0].strip();
        try {
            return switch (command) {
                case "USER CREATE" -> {
                    requireArgs(parts, 8, "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>");
                    userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    yield "USER CREATED";
                }
                case "USER DELETE" -> {
                    requireArgs(parts, 3, "USER DELETE;<email>;<password>");
                    userService.delete(parts[1], parts[2]);
                    yield "USER DELETED";
                }
                case "USER UPDATE" -> {
                    requireArgs(parts, 5, "USER UPDATE;<email>;<password>;<field>;<value>");
                    userService.update(parts[1], parts[2], parts[3], parts[4]);
                    yield "USER UPDATED";
                }
                case "HELP" -> {
                    requireArgs(parts, 1, "HELP");
                    yield HELP_TEXT;
                }
                case "EXIT" -> {
                    requireArgs(parts, 1, "EXIT");
                    exitRequested = true;
                    yield "BYE";
                }
                default -> "ERROR: unknown command: " + command;
            };
        } catch (UserOperationException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: command could not be processed: " + e.getMessage();
        }
    }

    private static void requireArgs(String[] parts, int expected, String syntax) throws UserOperationException {
        if (parts.length != expected) {
            throw new UserOperationException("wrong number of arguments, expected " + syntax);
        }
    }
}
