package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Processes text commands one line at a time. The same instance and logic are
 * used for every input source (standard input or a command file).
 */
public class CommandProcessor {

    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "HELP",
            "EXIT");

    private final PrintStream out;
    private final ParkController park = ParkController.getInstance();

    public CommandProcessor(PrintStream out) {
        this.out = out;
    }

    /**
     * Processes every line of the given source until the end of the input or
     * until an EXIT command is executed.
     */
    public void run(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            boolean keepGoing;
            try {
                keepGoing = execute(line);
            } catch (RuntimeException e) {
                out.println("Error: could not process command: " + line.trim());
                keepGoing = true;
            }
            if (!keepGoing) {
                return;
            }
        }
    }

    /**
     * Executes a single command line. Fields are separated by ';' and the first
     * field is the command keyword.
     *
     * @return {@code false} when processing must stop (EXIT), {@code true} otherwise
     */
    public boolean execute(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        String[] fields = command.split(";", -1);
        String keyword = fields[0].trim();
        try {
            return dispatch(command, keyword, fields);
        } catch (IllegalArgumentException e) {
            out.println("Error: " + e.getMessage());
            return true;
        }
    }

    private boolean dispatch(String command, String keyword, String[] fields) {
        switch (keyword) {
            case "HELP":
                if (rejectArguments(keyword, fields)) {
                    return true;
                }
                printHelp();
                return true;
            case "EXIT":
                if (rejectArguments(keyword, fields)) {
                    return true;
                }
                out.println("Exiting application.");
                return false;
            case "SYSTEM DATE":
                if (requireArguments(keyword, fields, 1)) {
                    park.setCurrentDate(fields[1]);
                    out.println("SYSTEM DATE UPDATED: " + park.getCurrentDate());
                }
                return true;
            case "SYSTEM PRICE":
                if (requireArguments(keyword, fields, 1)) {
                    park.setBasePrice(fields[1]);
                    out.println("SYSTEM PRICE UPDATED: " + park.getBasePrice());
                }
                return true;
            case "GROUP CREATE":
                if (requireArguments(keyword, fields, 2)) {
                    park.createGroup(fields[1], fields[2]);
                    out.println("GROUP CREATED");
                }
                return true;
            case "GROUP ADD":
                if (requireArguments(keyword, fields, 2)) {
                    park.addUserToGroup(fields[1], fields[2]);
                    out.println("USER ADDED TO GROUP");
                }
                return true;
            case "GROUP REMOVE":
                if (requireArguments(keyword, fields, 2)) {
                    park.removeUserFromGroup(fields[1], fields[2]);
                    out.println("USER REMOVED FROM GROUP");
                }
                return true;
            case "GROUP DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.deleteGroup(fields[1]);
                    out.println("GROUP DELETED");
                }
                return true;
            default:
                out.println("Error: unknown command: " + command);
                return true;
        }
    }

    private boolean requireArguments(String keyword, String[] fields, int expected) {
        if (fields.length == expected + 1) {
            return true;
        }
        out.println("Error: invalid arguments for " + keyword + " (expected " + expected + ")");
        return false;
    }

    private boolean rejectArguments(String keyword, String[] fields) {
        if (fields.length == 1) {
            return false;
        }
        out.println("Error: invalid arguments for " + keyword + " (expected no arguments)");
        return true;
    }

    private void printHelp() {
        out.println("Available commands:");
        for (String syntax : COMMAND_SYNTAX) {
            out.println("  " + syntax);
        }
    }
}
