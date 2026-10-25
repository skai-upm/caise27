package es.upm.etsisi.park.command;

import es.upm.etsisi.park.controller.ParkController;
import es.upm.etsisi.park.model.Attraction;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.PersistenceException;
import es.upm.etsisi.park.service.UserException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses ';'-separated command lines and dispatches them. Used identically by every input source.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String NEWLINE = System.lineSeparator();

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

    static final String USER_CREATE_SYNTAX =
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>";
    static final String USER_DELETE_SYNTAX = "USER DELETE;<Email>;<Password>";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>";
    static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;<yyyy-MM-dd>";
    static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;<Price>";
    static final String GROUP_CREATE_SYNTAX = "GROUP CREATE;<Identifier>;<Name>";
    static final String GROUP_ADD_SYNTAX = "GROUP ADD;<Identifier>;<Email>";
    static final String GROUP_REMOVE_SYNTAX = "GROUP REMOVE;<Identifier>;<Email>";
    static final String GROUP_DELETE_SYNTAX = "GROUP DELETE;<Identifier>";
    static final String TICKET_CREATE_SYNTAX = "TICKET CREATE;<identifier>;<email>;<date>";
    static final String TICKET_DELETE_SYNTAX = "TICKET DELETE;<identifier>";
    static final String GROUPTICKET_CREATE_SYNTAX = "GROUPTICKET CREATE;<identifier>;<group>;<date>";
    static final String GROUPTICKET_DELETE_SYNTAX = "GROUPTICKET DELETE;<identifier>";
    static final String ATTRACTION_CREATE_SYNTAX = "ATTRACTION CREATE;<identifier>;<name>;<capacity>";
    static final String ATTRACTION_ENTER_SYNTAX = "ATTRACTION ENTER;<attraction>;<ticket>";
    static final String ATTRACTION_EXIT_SYNTAX = "ATTRACTION EXIT;<attraction>;<email>";
    static final String ATTRACTION_LIST_SYNTAX = "ATTRACTION LIST;<attraction>";
    static final String ATTRACTION_STATUS_SYNTAX = "ATTRACTION STATUS;<attractionIdentifier>";

    static final List<String> ALL_SYNTAX = List.of(
            USER_CREATE_SYNTAX, USER_DELETE_SYNTAX, USER_UPDATE_SYNTAX,
            SYSTEM_DATE_SYNTAX, SYSTEM_PRICE_SYNTAX,
            GROUP_CREATE_SYNTAX, GROUP_ADD_SYNTAX, GROUP_REMOVE_SYNTAX, GROUP_DELETE_SYNTAX,
            TICKET_CREATE_SYNTAX, TICKET_DELETE_SYNTAX,
            GROUPTICKET_CREATE_SYNTAX, GROUPTICKET_DELETE_SYNTAX,
            ATTRACTION_CREATE_SYNTAX, ATTRACTION_ENTER_SYNTAX, ATTRACTION_EXIT_SYNTAX,
            ATTRACTION_LIST_SYNTAX, ATTRACTION_STATUS_SYNTAX,
            HELP, EXIT);

    private final ParkController park;

    public CommandProcessor(ParkController park) {
        this.park = park;
    }

    public CommandResult process(String line) {
        if (line == null || line.isBlank()) {
            return CommandResult.of("");
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = normalizeKeyword(parts[0]);
        try {
            return dispatch(command, parts);
        } catch (UserException | PersistenceException e) {
            return CommandResult.error(e.getMessage());
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected error processing command: " + e.getMessage());
        }
    }

    private CommandResult dispatch(String command, String[] parts) {
        return switch (command) {
            case USER_CREATE -> userCreate(parts);
            case USER_DELETE -> userDelete(parts);
            case USER_UPDATE -> userUpdate(parts);
            case SYSTEM_DATE -> systemDate(parts);
            case SYSTEM_PRICE -> systemPrice(parts);
            case GROUP_CREATE -> groupCreate(parts);
            case GROUP_ADD -> groupAdd(parts);
            case GROUP_REMOVE -> groupRemove(parts);
            case GROUP_DELETE -> groupDelete(parts);
            case TICKET_CREATE -> ticketCreate(parts);
            case TICKET_DELETE -> ticketDelete(parts);
            case GROUPTICKET_CREATE -> groupTicketCreate(parts);
            case GROUPTICKET_DELETE -> groupTicketDelete(parts);
            case ATTRACTION_CREATE -> attractionCreate(parts);
            case ATTRACTION_ENTER -> attractionEnter(parts);
            case ATTRACTION_EXIT -> attractionExit(parts);
            case ATTRACTION_LIST -> attractionList(parts);
            case ATTRACTION_STATUS -> attractionStatus(parts);
            case HELP -> help();
            case EXIT -> CommandResult.exit("BYE");
            default -> CommandResult.error("Unknown command '" + parts[0] + "'. Type HELP to list commands");
        };
    }

    // ----- Users -----

    private CommandResult userCreate(String[] parts) {
        requireArguments(parts, 7, USER_CREATE_SYNTAX);
        park.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return CommandResult.of("USER CREATED");
    }

    private CommandResult userDelete(String[] parts) {
        requireArguments(parts, 2, USER_DELETE_SYNTAX);
        park.deleteUser(parts[1], parts[2]);
        return CommandResult.of("USER DELETED");
    }

    private CommandResult userUpdate(String[] parts) {
        requireArguments(parts, 4, USER_UPDATE_SYNTAX);
        park.updateUser(parts[1], parts[2], parts[3], parts[4]);
        return CommandResult.of("USER UPDATED");
    }

    // ----- System -----

    private CommandResult systemDate(String[] parts) {
        requireArguments(parts, 1, SYSTEM_DATE_SYNTAX);
        return CommandResult.of("SYSTEM DATE SET TO " + park.setCurrentDate(parts[1]));
    }

    private CommandResult systemPrice(String[] parts) {
        requireArguments(parts, 1, SYSTEM_PRICE_SYNTAX);
        return CommandResult.of("SYSTEM PRICE SET TO " + park.setCurrentPrice(parts[1]).toPlainString());
    }

    // ----- Groups -----

    private CommandResult groupCreate(String[] parts) {
        requireArguments(parts, 2, GROUP_CREATE_SYNTAX);
        park.createGroup(parts[1], parts[2]);
        return CommandResult.of("GROUP CREATED");
    }

    private CommandResult groupAdd(String[] parts) {
        requireArguments(parts, 2, GROUP_ADD_SYNTAX);
        park.addToGroup(parts[1], parts[2]);
        return CommandResult.of("USER ADDED TO GROUP");
    }

    private CommandResult groupRemove(String[] parts) {
        requireArguments(parts, 2, GROUP_REMOVE_SYNTAX);
        park.removeFromGroup(parts[1], parts[2]);
        return CommandResult.of("USER REMOVED FROM GROUP");
    }

    private CommandResult groupDelete(String[] parts) {
        requireArguments(parts, 1, GROUP_DELETE_SYNTAX);
        park.deleteGroup(parts[1]);
        return CommandResult.of("GROUP DELETED");
    }

    // ----- Tickets -----

    private CommandResult ticketCreate(String[] parts) {
        requireArguments(parts, 3, TICKET_CREATE_SYNTAX);
        IndividualTicket ticket = park.createTicket(parts[1], parts[2], parts[3]);
        return CommandResult.of("TICKET CREATED. PRICE: " + ticket.getPrice().toPlainString() + " EUR");
    }

    private CommandResult ticketDelete(String[] parts) {
        requireArguments(parts, 1, TICKET_DELETE_SYNTAX);
        park.deleteTicket(parts[1]);
        return CommandResult.of("TICKET DELETED");
    }

    private CommandResult groupTicketCreate(String[] parts) {
        requireArguments(parts, 3, GROUPTICKET_CREATE_SYNTAX);
        GroupTicket ticket = park.createGroupTicket(parts[1], parts[2], parts[3]);
        return CommandResult.of("GROUP TICKET CREATED. MEMBERS: " + ticket.getTickets().size()
                + ", DISCOUNT: " + ticket.getDiscount().getPercent() + "%, TOTAL PRICE: "
                + ticket.getPrice().toPlainString() + " EUR");
    }

    private CommandResult groupTicketDelete(String[] parts) {
        requireArguments(parts, 1, GROUPTICKET_DELETE_SYNTAX);
        park.deleteGroupTicket(parts[1]);
        return CommandResult.of("GROUP TICKET DELETED");
    }

    // ----- Attractions -----

    private CommandResult attractionCreate(String[] parts) {
        requireArguments(parts, 3, ATTRACTION_CREATE_SYNTAX);
        park.createAttraction(parts[1], parts[2], parts[3]);
        return CommandResult.of("ATTRACTION CREATED");
    }

    private CommandResult attractionEnter(String[] parts) {
        requireArguments(parts, 2, ATTRACTION_ENTER_SYNTAX);
        List<User> entered = park.enterAttraction(parts[1], parts[2]);
        return CommandResult.of(entered.size() + " USER(S) ENTERED " + parts[1]);
    }

    private CommandResult attractionExit(String[] parts) {
        requireArguments(parts, 2, ATTRACTION_EXIT_SYNTAX);
        park.exitAttraction(parts[1], parts[2]);
        return CommandResult.of("USER LEFT " + parts[1]);
    }

    private CommandResult attractionList(String[] parts) {
        requireArguments(parts, 1, ATTRACTION_LIST_SYNTAX);
        Attraction attraction = park.requireAttraction(parts[1]);
        if (attraction.getOccupants().isEmpty()) {
            return CommandResult.of("NO USERS IN " + attraction.getId());
        }
        return CommandResult.of(String.join(NEWLINE, emails(attraction)));
    }

    private CommandResult attractionStatus(String[] parts) {
        requireArguments(parts, 1, ATTRACTION_STATUS_SYNTAX);
        Attraction attraction = park.requireAttraction(parts[1]);
        List<String> occupants = emails(attraction);
        String text = String.join(NEWLINE,
                "ATTRACTION: " + attraction.getId(),
                "NAME: " + attraction.getName(),
                "CAPACITY: " + attraction.getCapacity(),
                "USERS INSIDE: " + attraction.getOccupancy(),
                "AVAILABLE PLACES: " + attraction.getAvailablePlaces(),
                "OCCUPANTS: " + (occupants.isEmpty() ? "-" : String.join(", ", occupants)));
        return CommandResult.of(text);
    }

    private static List<String> emails(Attraction attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    // ----- General -----

    private static CommandResult help() {
        StringBuilder text = new StringBuilder("Available commands:");
        for (String syntax : ALL_SYNTAX) {
            text.append(NEWLINE).append("  ").append(syntax);
        }
        text.append(NEWLINE).append("  (USER UPDATE fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        return CommandResult.of(text.toString());
    }

    private static void requireArguments(String[] parts, int count, String syntax) {
        if (parts.length != count + 1) {
            throw new UserException("Wrong number of arguments. Usage: " + syntax);
        }
    }

    private static String normalizeKeyword(String keyword) {
        StringBuilder sb = new StringBuilder();
        for (String word : keyword.trim().split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(word.toUpperCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
