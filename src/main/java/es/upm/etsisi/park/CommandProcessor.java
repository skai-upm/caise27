package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses and executes text commands. Interactive and file input both go through
 * {@link #run(BufferedReader, PrintStream)}, so a command behaves the same whatever its source.
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

    private static final String SEPARATOR = ";";

    private final UserService users;
    private boolean exitRequested;

    public CommandProcessor(UserService users) {
        this.users = users;
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
            output.println(execute(line));
        }
    }

    /**
     * Processes the commands stored in a text file, one per line.
     *
     * @throws IOException if the file does not exist, is not a regular file or cannot be read
     */
    public void runFile(Path commands, PrintStream output) throws IOException {
        if (!Files.isRegularFile(commands) || !Files.isReadable(commands)) {
            throw new IOException("Cannot read command file: " + commands);
        }
        try (BufferedReader reader = Files.newBufferedReader(commands, StandardCharsets.UTF_8)) {
            run(reader, output);
        }
    }

    /**
     * Executes one command and returns the text to show. Never throws for bad input.
     */
    public String execute(String line) {
        String[] parts = line.trim().split(SEPARATOR, -1);
        try {
            return switch (parts[0]) {
                case "USER CREATE" -> create(parts);
                case "USER DELETE" -> delete(parts);
                case "USER UPDATE" -> update(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR: Unknown command: " + parts[0];
            };
        } catch (UserException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: Unexpected failure: " + e.getMessage();
        }
    }

    private String create(String[] p) {
        expectFields(p, 8);
        users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "USER CREATED";
    }

    private String delete(String[] p) {
        expectFields(p, 3);
        users.delete(p[1], p[2]);
        return "USER DELETED";
    }

    private String update(String[] p) {
        expectFields(p, 5);
        users.update(p[1], p[2], p[3], p[4]);
        return "USER UPDATED";
    }

    private String help(String[] p) {
        expectFields(p, 1);
        return HELP_TEXT;
    }

    private String exit(String[] p) {
        expectFields(p, 1);
        exitRequested = true;
        return "BYE";
    }

    private static void expectFields(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new UserException("Malformed command: expected " + (expected - 1)
                    + " parameter(s) but got " + (parts.length - 1));
        }
    }
}
