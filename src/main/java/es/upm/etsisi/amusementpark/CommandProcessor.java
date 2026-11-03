package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses and executes ';'-separated commands. Interactive input and command
 * files are both processed by {@link #run(BufferedReader)}, so a command
 * behaves the same whatever its source.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "  USER DELETE;<email>;<password>",
            "  USER UPDATE;<email>;<password>;<field>;<value>",
            "      <field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD",
            "  HELP",
            "  EXIT");

    private final UserService userService;
    private final PrintStream out;

    public CommandProcessor(UserService userService, PrintStream out) {
        this.userService = userService;
        this.out = out;
    }

    /** Processes every line until EXIT or end of input; blank lines are ignored. */
    public void run(BufferedReader input) throws IOException {
        String line;
        boolean running = true;
        while (running && (line = input.readLine()) != null) {
            if (!line.isBlank()) {
                running = execute(line);
            }
        }
    }

    /** Processes the commands stored in a text file, one command per line. */
    public void runFile(Path commandFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            run(reader);
        }
    }

    /**
     * Executes one command line and prints its result.
     *
     * @return false when the command was EXIT, true otherwise
     */
    public boolean execute(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String keyword = parts[0].trim();
        try {
            switch (keyword) {
                case "USER CREATE" -> create(parts);
                case "USER DELETE" -> delete(parts);
                case "USER UPDATE" -> update(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> {
                    expectArguments(parts, 1, "EXIT");
                    out.println("BYE");
                    return false;
                }
                default -> throw new UserException("unknown command '" + keyword + "'. Type HELP for the list of commands");
            }
        } catch (UserException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void create(String[] parts) throws UserException {
        expectArguments(parts, 8, "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>");
        userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        out.println("USER CREATED");
    }

    private void delete(String[] parts) throws UserException {
        expectArguments(parts, 3, "USER DELETE;<email>;<password>");
        userService.delete(parts[1], parts[2]);
        out.println("USER DELETED");
    }

    private void update(String[] parts) throws UserException {
        expectArguments(parts, 5, "USER UPDATE;<email>;<password>;<field>;<value>");
        userService.update(parts[1], parts[2], parts[3], parts[4]);
        out.println("USER UPDATED");
    }

    private void help(String[] parts) throws UserException {
        expectArguments(parts, 1, "HELP");
        out.println(HELP_TEXT);
    }

    private static void expectArguments(String[] parts, int expected, String syntax) throws UserException {
        if (parts.length != expected) {
            throw new UserException("wrong number of arguments. Usage: " + syntax);
        }
    }
}
