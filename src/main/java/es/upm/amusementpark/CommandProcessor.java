package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attraction>",
            "HELP",
            "EXIT");

    private final PrintStream out;
    private final ParkController park = ParkController.getInstance();

    private Runnable afterCommand = () -> { };

    public CommandProcessor(PrintStream out) {
        this.out = out;
    }

    /** Action run after every processed line (for example, saving the park state). */
    public void setAfterCommand(Runnable afterCommand) {
        this.afterCommand = afterCommand;
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
            afterCommand.run();
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
            case "TICKET CREATE":
                if (requireArguments(keyword, fields, 3)) {
                    Ticket ticket = park.createIndividualTicket(fields[1], fields[2], fields[3]);
                    out.println("TICKET CREATED. PRICE: " + ticket.getPrice() + " EUR");
                }
                return true;
            case "TICKET DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.deleteTicket(fields[1]);
                    out.println("TICKET DELETED");
                }
                return true;
            case "GROUPTICKET CREATE":
                if (requireArguments(keyword, fields, 3)) {
                    Ticket ticket = park.createGroupTicket(fields[1], fields[2], fields[3]);
                    out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice() + " EUR");
                }
                return true;
            case "GROUPTICKET DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.deleteGroupTicket(fields[1]);
                    out.println("GROUP TICKET DELETED");
                }
                return true;
            case "ATTRACTION CREATE":
                if (requireArguments(keyword, fields, 3)) {
                    park.createAttraction(fields[1], fields[2], fields[3]);
                    out.println("ATTRACTION CREATED");
                }
                return true;
            case "ATTRACTION ENTER":
                if (requireArguments(keyword, fields, 2)) {
                    for (User user : park.enterAttraction(fields[1], fields[2])) {
                        out.println("USER " + user.getEmail() + " ENTERED ATTRACTION " + fields[1].trim());
                    }
                }
                return true;
            case "ATTRACTION EXIT":
                if (requireArguments(keyword, fields, 2)) {
                    User user = park.exitAttraction(fields[1], fields[2]);
                    out.println("USER " + user.getEmail() + " LEFT ATTRACTION " + fields[1].trim());
                }
                return true;
            case "ATTRACTION STATUS":
                if (requireArguments(keyword, fields, 1)) {
                    printStatus(park.existingAttraction(fields[1]));
                }
                return true;
            case "ATTRACTION LIST":
                if (requireArguments(keyword, fields, 1)) {
                    listOccupants(park.existingAttraction(fields[1]));
                }
                return true;
            default:
                out.println("Error: unknown command: " + command);
                return true;
        }
    }

    private void printStatus(Attraction attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        out.println("ATTRACTION: " + attraction.getIdentifier());
        out.println("NAME: " + attraction.getName());
        out.println("CAPACITY: " + attraction.getCapacity());
        out.println("USERS INSIDE: " + emails.size());
        out.println("AVAILABLE PLACES: " + attraction.getAvailablePlaces());
        out.println("OCCUPANTS: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }

    private void listOccupants(Attraction attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("NO USERS IN ATTRACTION " + attraction.getIdentifier());
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
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
