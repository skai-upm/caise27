package es.upm.etsisi.park.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.Park;
import es.upm.etsisi.park.ParkException;
import es.upm.etsisi.park.model.attraction.Attraction;
import es.upm.etsisi.park.model.ticket.GroupTicket;
import es.upm.etsisi.park.model.ticket.IndividualTicket;
import es.upm.etsisi.park.model.ticket.Money;
import es.upm.etsisi.park.user.User;
import es.upm.etsisi.park.user.UserField;

/**
 * Interprets one text command per line. Elements are separated by ';'; the
 * first element is the command keyword and the rest are its arguments.
 * The same processor is used for interactive and file input; the work is
 * delegated to the central {@link Park}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    static final String USER_CREATE = "USER CREATE";
    static final String USER_DELETE = "USER DELETE";
    static final String USER_UPDATE = "USER UPDATE";
    static final String SYSTEM_DATE = "SYSTEM DATE";
    static final String SYSTEM_PRICE = "SYSTEM PRICE";
    static final String GROUP_CREATE = "GROUP CREATE";
    static final String GROUP_ADD = "GROUP ADD";
    static final String GROUP_REMOVE = "GROUP REMOVE";
    static final String GROUP_DELETE = "GROUP DELETE";
    static final String TICKET_CREATE = "TICKET CREATE";
    static final String TICKET_DELETE = "TICKET DELETE";
    static final String GROUPTICKET_CREATE = "GROUPTICKET CREATE";
    static final String GROUPTICKET_DELETE = "GROUPTICKET DELETE";
    static final String ATTRACTION_CREATE = "ATTRACTION CREATE";
    static final String ATTRACTION_ENTER = "ATTRACTION ENTER";
    static final String ATTRACTION_EXIT = "ATTRACTION EXIT";
    static final String ATTRACTION_LIST = "ATTRACTION LIST";
    static final String ATTRACTION_STATUS = "ATTRACTION STATUS";
    static final String HELP = "HELP";
    static final String EXIT = "EXIT";

    private static final String CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    private static final String DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    private static final String UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";
    private static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;<yyyy-MM-dd>";
    private static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;<price>";
    private static final String GROUP_CREATE_SYNTAX = "GROUP CREATE;<identifier>;<name>";
    private static final String GROUP_ADD_SYNTAX = "GROUP ADD;<identifier>;<email>";
    private static final String GROUP_REMOVE_SYNTAX = "GROUP REMOVE;<identifier>;<email>";
    private static final String GROUP_DELETE_SYNTAX = "GROUP DELETE;<identifier>";
    private static final String TICKET_CREATE_SYNTAX = "TICKET CREATE;<identifier>;<email>;<date>";
    private static final String TICKET_DELETE_SYNTAX = "TICKET DELETE;<identifier>";
    private static final String GROUPTICKET_CREATE_SYNTAX = "GROUPTICKET CREATE;<identifier>;<group>;<date>";
    private static final String GROUPTICKET_DELETE_SYNTAX = "GROUPTICKET DELETE;<identifier>";
    private static final String ATTRACTION_CREATE_SYNTAX = "ATTRACTION CREATE;<identifier>;<name>;<capacity>";
    private static final String ATTRACTION_ENTER_SYNTAX = "ATTRACTION ENTER;<attraction>;<ticket>";
    private static final String ATTRACTION_EXIT_SYNTAX = "ATTRACTION EXIT;<attraction>;<email>";
    private static final String ATTRACTION_LIST_SYNTAX = "ATTRACTION LIST;<attraction>";
    private static final String ATTRACTION_STATUS_SYNTAX = "ATTRACTION STATUS;<attraction>";

    private final Park park;
    private final PrintStream out;

    public CommandProcessor(Park park, PrintStream out) {
        this.park = park;
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
                case SYSTEM_DATE -> {
                    requireArgs(args, 1, SYSTEM_DATE_SYNTAX);
                    out.println("SYSTEM DATE SET TO " + park.setSystemDate(args[0]));
                }
                case SYSTEM_PRICE -> {
                    requireArgs(args, 1, SYSTEM_PRICE_SYNTAX);
                    out.println("SYSTEM PRICE SET TO " + Money.format(park.setBasePrice(args[0])));
                }
                case GROUP_CREATE -> {
                    requireArgs(args, 2, GROUP_CREATE_SYNTAX);
                    park.createGroup(args[0], args[1]);
                    out.println("GROUP CREATED");
                }
                case GROUP_ADD -> {
                    requireArgs(args, 2, GROUP_ADD_SYNTAX);
                    park.addToGroup(args[0], args[1]);
                    out.println("USER ADDED TO GROUP");
                }
                case GROUP_REMOVE -> {
                    requireArgs(args, 2, GROUP_REMOVE_SYNTAX);
                    park.removeFromGroup(args[0], args[1]);
                    out.println("USER REMOVED FROM GROUP");
                }
                case GROUP_DELETE -> {
                    requireArgs(args, 1, GROUP_DELETE_SYNTAX);
                    park.deleteGroup(args[0]);
                    out.println("GROUP DELETED");
                }
                case TICKET_CREATE -> createTicket(args);
                case TICKET_DELETE -> {
                    requireArgs(args, 1, TICKET_DELETE_SYNTAX);
                    park.deleteTicket(args[0]);
                    out.println("TICKET DELETED");
                }
                case GROUPTICKET_CREATE -> createGroupTicket(args);
                case GROUPTICKET_DELETE -> {
                    requireArgs(args, 1, GROUPTICKET_DELETE_SYNTAX);
                    park.deleteGroupTicket(args[0]);
                    out.println("GROUP TICKET DELETED");
                }
                case ATTRACTION_CREATE -> {
                    requireArgs(args, 3, ATTRACTION_CREATE_SYNTAX);
                    park.createAttraction(args[0], args[1], args[2]);
                    out.println("ATTRACTION CREATED");
                }
                case ATTRACTION_ENTER -> {
                    requireArgs(args, 2, ATTRACTION_ENTER_SYNTAX);
                    int entered = park.enterAttraction(args[0], args[1]);
                    out.println("ATTRACTION ENTERED: " + entered + " user(s) entered "
                            + park.requireAttraction(args[0]).getId());
                }
                case ATTRACTION_EXIT -> {
                    requireArgs(args, 2, ATTRACTION_EXIT_SYNTAX);
                    User user = park.exitAttraction(args[0], args[1]);
                    out.println("ATTRACTION EXITED: " + user.getEmail() + " left "
                            + park.requireAttraction(args[0]).getId());
                }
                case ATTRACTION_LIST -> {
                    requireArgs(args, 1, ATTRACTION_LIST_SYNTAX);
                    listAttraction(park.requireAttraction(args[0]));
                }
                case ATTRACTION_STATUS -> {
                    requireArgs(args, 1, ATTRACTION_STATUS_SYNTAX);
                    printStatus(park.requireAttraction(args[0]));
                }
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
        } catch (ParkException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Could not process command: " + e.getMessage());
        }
        return true;
    }

    private void createUser(String[] args) throws ParkException {
        requireArgs(args, 7, CREATE_SYNTAX);
        park.createUser(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        out.println("USER CREATED");
    }

    private void deleteUser(String[] args) throws ParkException {
        requireArgs(args, 2, DELETE_SYNTAX);
        park.deleteUser(args[0], args[1]);
        out.println("USER DELETED");
    }

    private void updateUser(String[] args) throws ParkException {
        requireArgs(args, 4, UPDATE_SYNTAX);
        UserField field = UserField.fromName(args[2]);
        if (field == null) {
            throw new ParkException("Field '" + args[2] + "' cannot be modified. Modifiable fields: "
                    + UserField.names());
        }
        park.updateUser(args[0], args[1], field, args[3]);
        out.println("USER UPDATED");
    }

    private void createTicket(String[] args) throws ParkException {
        requireArgs(args, 3, TICKET_CREATE_SYNTAX);
        IndividualTicket ticket = park.createTicket(args[0], args[1], args[2]);
        out.println("TICKET CREATED: " + ticket.getId() + " for " + ticket.getDate() + ", price "
                + ticket.getFormattedPrice());
    }

    private void createGroupTicket(String[] args) throws ParkException {
        requireArgs(args, 3, GROUPTICKET_CREATE_SYNTAX);
        GroupTicket ticket = park.createGroupTicket(args[0], args[1], args[2]);
        out.println("GROUP TICKET CREATED: " + ticket.getId() + " for " + ticket.getDate() + ", "
                + ticket.getMembers().size() + " member(s), " + ticket.getDiscountPercent() + "% discount, total "
                + ticket.getFormattedPrice());
    }

    private void listAttraction(Attraction attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("ATTRACTION " + attraction.getId() + " IS EMPTY");
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    private void printStatus(Attraction attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        out.println("Attraction: " + attraction.getId());
        out.println("Name: " + attraction.getName());
        out.println("Capacity: " + attraction.getCapacity());
        out.println("Inside: " + attraction.getOccupants().size());
        out.println("Available: " + attraction.getAvailable());
        out.println("Users: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }

    private void printHelp() {
        out.println("Available commands:");
        out.println("  " + CREATE_SYNTAX);
        out.println("  " + DELETE_SYNTAX);
        out.println("  " + UPDATE_SYNTAX);
        out.println("      <field> is one of: " + UserField.names());
        out.println("  " + SYSTEM_DATE_SYNTAX);
        out.println("  " + SYSTEM_PRICE_SYNTAX);
        out.println("  " + GROUP_CREATE_SYNTAX);
        out.println("  " + GROUP_ADD_SYNTAX);
        out.println("  " + GROUP_REMOVE_SYNTAX);
        out.println("  " + GROUP_DELETE_SYNTAX);
        out.println("  " + TICKET_CREATE_SYNTAX);
        out.println("  " + TICKET_DELETE_SYNTAX);
        out.println("  " + GROUPTICKET_CREATE_SYNTAX);
        out.println("  " + GROUPTICKET_DELETE_SYNTAX);
        out.println("  " + ATTRACTION_CREATE_SYNTAX);
        out.println("  " + ATTRACTION_ENTER_SYNTAX);
        out.println("  " + ATTRACTION_EXIT_SYNTAX);
        out.println("  " + ATTRACTION_LIST_SYNTAX);
        out.println("  " + ATTRACTION_STATUS_SYNTAX);
        out.println("  " + HELP);
        out.println("  " + EXIT);
    }

    private void error(String message) {
        out.println("ERROR: " + message);
    }

    private static void requireArgs(String[] args, int expected, String syntax) throws ParkException {
        if (args.length != expected) {
            throw new ParkException("Wrong number of arguments (" + args.length + ", expected " + expected
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
