package es.upm.etsisi.amusementpark.command;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.park.Park;
import es.upm.etsisi.amusementpark.park.ParkException;
import es.upm.etsisi.amusementpark.park.Prices;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserException;
import es.upm.etsisi.amusementpark.user.UserField;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Interprets single {@code ;}-separated command lines and delegates to the central park.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "  <field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
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
            "ATTRACTION STATUS;<attractionIdentifier>",
            "HELP",
            "EXIT");

    private static final String SEPARATOR = ";";
    private static final String NL = System.lineSeparator();

    private final Park park;

    public CommandProcessor(Park park) {
        this.park = park;
    }

    public CommandResult process(String line) {
        String[] p = line.trim().split(SEPARATOR, -1);
        try {
            return switch (p[0]) {
                case "USER CREATE" -> createUser(p);
                case "USER DELETE" -> deleteUser(p);
                case "USER UPDATE" -> updateUser(p);
                case "SYSTEM DATE" -> systemDate(p);
                case "SYSTEM PRICE" -> systemPrice(p);
                case "GROUP CREATE" -> groupCreate(p);
                case "GROUP ADD" -> groupAdd(p);
                case "GROUP REMOVE" -> groupRemove(p);
                case "GROUP DELETE" -> groupDelete(p);
                case "TICKET CREATE" -> ticketCreate(p);
                case "TICKET DELETE" -> ticketDelete(p);
                case "GROUPTICKET CREATE" -> groupTicketCreate(p);
                case "GROUPTICKET DELETE" -> groupTicketDelete(p);
                case "ATTRACTION CREATE" -> attractionCreate(p);
                case "ATTRACTION ENTER" -> attractionEnter(p);
                case "ATTRACTION EXIT" -> attractionExit(p);
                case "ATTRACTION LIST" -> attractionList(p);
                case "ATTRACTION STATUS" -> attractionStatus(p);
                case "HELP" -> expectArity(p, 1, "HELP", CommandResult.message(HELP_TEXT));
                case "EXIT" -> expectArity(p, 1, "EXIT", new CommandResult("BYE", true));
                default -> CommandResult.error("unknown command '" + p[0] + "' (type HELP)");
            };
        } catch (UserException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    // ---- users -----------------------------------------------------------------------

    private CommandResult createUser(String[] p) throws UserException {
        requireArity(p, 8, "USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card");
        park.createUser(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        return CommandResult.message("USER CREATED");
    }

    private CommandResult deleteUser(String[] p) throws UserException {
        requireArity(p, 3, "USER DELETE;Email;Password");
        park.deleteUser(p[1], p[2]);
        return CommandResult.message("USER DELETED");
    }

    private CommandResult updateUser(String[] p) throws UserException {
        requireArity(p, 5, "USER UPDATE;Email;Password;Field;NewValue");
        Optional<UserField> field = UserField.fromName(p[3]);
        if (field.isEmpty()) {
            throw new UserException("field '" + p[3] + "' cannot be modified");
        }
        park.updateUser(p[1], p[2], field.get(), p[4]);
        return CommandResult.message("USER UPDATED");
    }

    // ---- system ----------------------------------------------------------------------

    private CommandResult systemDate(String[] p) throws UserException {
        requireArity(p, 2, "SYSTEM DATE;<yyyy-MM-dd>");
        park.setCurrentDate(parseDate(p[1]));
        return CommandResult.message("SYSTEM DATE SET TO " + park.getCurrentDate());
    }

    private CommandResult systemPrice(String[] p) throws UserException {
        requireArity(p, 2, "SYSTEM PRICE;<Price>");
        BigDecimal price = Prices.parsePositive(p[1]);
        if (price == null) {
            throw new ParkException("invalid price '" + p[1] + "', expected a positive amount such as 45.50");
        }
        park.setBasePrice(price);
        return CommandResult.message("SYSTEM PRICE SET TO " + Prices.format(price));
    }

    // ---- groups ----------------------------------------------------------------------

    private CommandResult groupCreate(String[] p) throws UserException {
        requireArity(p, 3, "GROUP CREATE;<Identifier>;<Name>");
        park.createGroup(p[1], p[2]);
        return CommandResult.message("GROUP CREATED");
    }

    private CommandResult groupAdd(String[] p) throws UserException {
        requireArity(p, 3, "GROUP ADD;<Identifier>;<Email>");
        park.addToGroup(p[1], p[2]);
        return CommandResult.message("USER ADDED TO GROUP");
    }

    private CommandResult groupRemove(String[] p) throws UserException {
        requireArity(p, 3, "GROUP REMOVE;<Identifier>;<Email>");
        park.removeFromGroup(p[1], p[2]);
        return CommandResult.message("USER REMOVED FROM GROUP");
    }

    private CommandResult groupDelete(String[] p) throws UserException {
        requireArity(p, 2, "GROUP DELETE;<Identifier>");
        park.deleteGroup(p[1]);
        return CommandResult.message("GROUP DELETED");
    }

    // ---- tickets ---------------------------------------------------------------------

    private CommandResult ticketCreate(String[] p) throws UserException {
        requireArity(p, 4, "TICKET CREATE;<identifier>;<email>;<date>");
        IndividualTicket ticket = park.createTicket(p[1], p[2], parseDate(p[3]));
        return CommandResult.message("TICKET CREATED; PRICE: " + Prices.format(ticket.getPrice()));
    }

    private CommandResult ticketDelete(String[] p) throws UserException {
        requireArity(p, 2, "TICKET DELETE;<identifier>");
        park.deleteTicket(p[1]);
        return CommandResult.message("TICKET DELETED");
    }

    private CommandResult groupTicketCreate(String[] p) throws UserException {
        requireArity(p, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
        GroupTicket ticket = park.createGroupTicket(p[1], p[2], parseDate(p[3]));
        return CommandResult.message("GROUP TICKET CREATED; MEMBERS: " + ticket.getIndividualTickets().size()
                + "; TOTAL PRICE: " + Prices.format(ticket.getPrice()));
    }

    private CommandResult groupTicketDelete(String[] p) throws UserException {
        requireArity(p, 2, "GROUPTICKET DELETE;<identifier>");
        park.deleteGroupTicket(p[1]);
        return CommandResult.message("GROUP TICKET DELETED");
    }

    // ---- attractions -----------------------------------------------------------------

    private CommandResult attractionCreate(String[] p) throws UserException {
        requireArity(p, 4, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
        int capacity;
        try {
            capacity = Integer.parseInt(p[3]);
        } catch (NumberFormatException e) {
            throw new ParkException("capacity must be a positive integer");
        }
        park.createAttraction(p[1], p[2], capacity);
        return CommandResult.message("ATTRACTION CREATED");
    }

    private CommandResult attractionEnter(String[] p) throws UserException {
        requireArity(p, 3, "ATTRACTION ENTER;<attraction>;<ticket>");
        List<User> entered = park.enter(p[1], p[2]);
        return CommandResult.message("ENTERED " + p[1] + ": " + emails(entered));
    }

    private CommandResult attractionExit(String[] p) throws UserException {
        requireArity(p, 3, "ATTRACTION EXIT;<attraction>;<email>");
        park.exit(p[1], p[2]);
        return CommandResult.message("USER LEFT " + p[1]);
    }

    private CommandResult attractionList(String[] p) throws UserException {
        requireArity(p, 2, "ATTRACTION LIST;<attraction>");
        List<User> occupants = park.requireAttraction(p[1]).getOccupants();
        if (occupants.isEmpty()) {
            return CommandResult.message("NO USERS IN " + p[1]);
        }
        return CommandResult.message(String.join(NL, occupants.stream().map(User::getEmail).toList()));
    }

    private CommandResult attractionStatus(String[] p) throws UserException {
        requireArity(p, 2, "ATTRACTION STATUS;<attractionIdentifier>");
        Attraction a = park.requireAttraction(p[1]);
        return CommandResult.message(String.join(NL,
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "CAPACITY: " + a.getCapacity(),
                "INSIDE: " + a.getOccupants().size(),
                "AVAILABLE: " + a.getFreePlaces(),
                "USERS: " + emails(a.getOccupants())));
    }

    // ---- helpers ---------------------------------------------------------------------

    private static String emails(List<User> users) {
        return users.isEmpty() ? "-" : String.join(", ", users.stream().map(User::getEmail).toList());
    }

    private static LocalDate parseDate(String text) throws ParkException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ParkException("invalid date '" + text + "', expected yyyy-MM-dd");
        }
    }

    private static CommandResult expectArity(String[] p, int n, String usage, CommandResult ok)
            throws UserException {
        requireArity(p, n, usage);
        return ok;
    }

    private static void requireArity(String[] p, int n, String usage) throws UserException {
        if (p.length != n) {
            throw new UserException("wrong number of arguments, usage: " + usage);
        }
    }
}
