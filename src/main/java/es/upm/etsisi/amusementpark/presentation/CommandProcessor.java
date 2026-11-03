package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.ParkTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/**
 * Interprets API-like text commands, split with {@link String#split}, and returns a one-line response
 * starting with {@code OK:} or {@code ERROR:}. Never throws for invalid input.
 * Presentation layer: it converts text into values and results into responses, and delegates every
 * operation to the business layer through {@link ParkController}.
 *
 * <pre>
 * USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta
 * USER DELETE;Correo;Contraseña
 * USER UPDATE;Correo;Contraseña;Campo;NuevoValor
 * GROUP CREATE;Id;Nombre
 * GROUP ADD;Id;Correo
 * GROUP REMOVE;Id;Correo
 * GROUP DELETE;Id
 * TICKET CREATE;Id;Correo;yyyy-MM-dd
 * TICKET DELETE;Id
 * GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd
 * GROUPTICKET DELETE;Id
 * ATTRACTION CREATE;Id;Nombre;Capacidad
 * ATTRACTION ENTER;IdAtraccion;IdTicket
 * ATTRACTION EXIT;IdAtraccion;IdTicket
 * ATTRACTION STATUS;IdAtraccion
 * SYSTEM DATE;yyyy-MM-dd
 * SYSTEM PRICE;Precio
 * HELP
 * EXIT
 * </pre>
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final int CREATE_TOKENS = 8;
    private static final int DELETE_TOKENS = 3;
    private static final int UPDATE_TOKENS = 5;
    private static final int DATE_TOKENS = 2;
    private static final int GROUP_CREATE_TOKENS = 3;
    private static final int GROUP_MEMBER_TOKENS = 3;
    private static final int GROUP_DELETE_TOKENS = 2;
    private static final int TICKET_CREATE_TOKENS = 4;
    private static final int TICKET_DELETE_TOKENS = 2;
    private static final int PRICE_TOKENS = 2;
    private static final int ATTRACTION_CREATE_TOKENS = 4;
    private static final int ATTRACTION_ACCESS_TOKENS = 3;
    private static final int ATTRACTION_STATUS_TOKENS = 2;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Syntax of every available command, shown by HELP. Extend it when new commands are added. */
    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;Id",
            "ATTRACTION CREATE;Id;Nombre;Capacidad",
            "ATTRACTION ENTER;IdAtraccion;IdTicket",
            "ATTRACTION EXIT;IdAtraccion;IdTicket",
            "ATTRACTION STATUS;IdAtraccion",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private final ParkController park;
    private final UserController users;
    private final GroupController groups;
    private final AttractionController attractions;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
        this.users = park.users();
        this.groups = park.groups();
        this.attractions = park.attractions();
    }

    public ParkController getPark() {
        return park;
    }

    /** True once EXIT has been processed: no further command must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return error("Empty command");
        }
        String[] tokens = line.split(PARAM_SEPARATOR, -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        String operation = tokens[0].toUpperCase(Locale.ROOT);
        try {
            return switch (operation) {
                case "USER CREATE" -> create(tokens);
                case "USER DELETE" -> delete(tokens);
                case "USER UPDATE" -> update(tokens);
                case "GROUP CREATE" -> groupCreate(tokens);
                case "GROUP ADD" -> groupAdd(tokens);
                case "GROUP REMOVE" -> groupRemove(tokens);
                case "GROUP DELETE" -> groupDelete(tokens);
                case "TICKET CREATE" -> ticketCreate(tokens);
                case "TICKET DELETE" -> ticketDelete(tokens);
                case "GROUPTICKET CREATE" -> groupTicketCreate(tokens);
                case "GROUPTICKET DELETE" -> groupTicketDelete(tokens);
                case "ATTRACTION CREATE" -> attractionCreate(tokens);
                case "ATTRACTION ENTER" -> attractionEnter(tokens);
                case "ATTRACTION EXIT" -> attractionExit(tokens);
                case "ATTRACTION STATUS" -> attractionStatus(tokens);
                case "SYSTEM DATE" -> systemDate(tokens);
                case "SYSTEM PRICE" -> systemPrice(tokens);
                case "HELP" -> tokens.length == 1 ? help() : usage("HELP");
                case "EXIT" -> tokens.length == 1 ? exit() : usage("EXIT");
                default -> error("Unknown command: " + tokens[0]);
            };
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Operation failed: " + e.getMessage());
        }
    }

    private String create(String[] t) {
        if (t.length != CREATE_TOKENS) {
            return usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        User u = users.create(t[1], t[2], t[3], t[4], t[5], t[6], t[7]);
        return "OK: User " + u.getEmail() + " created";
    }

    private String delete(String[] t) {
        if (t.length != DELETE_TOKENS) {
            return usage("USER DELETE;Correo;Contraseña");
        }
        park.deleteUser(t[1], t[2]);
        return "OK: User " + t[1] + " deleted";
    }

    private String update(String[] t) {
        if (t.length != UPDATE_TOKENS) {
            return usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field = UserField.parse(t[3]);
        if (field == null) {
            return error("Field " + t[3] + " cannot be updated (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        }
        users.update(t[1], t[2], field, t[4]);
        return "OK: User " + t[1] + " updated (" + field + ")";
    }

    private String groupCreate(String[] t) {
        if (t.length != GROUP_CREATE_TOKENS) {
            return usage("GROUP CREATE;Id;Nombre");
        }
        groups.create(t[1], t[2]);
        return "OK: Group " + t[1] + " created";
    }

    private String groupAdd(String[] t) {
        if (t.length != GROUP_MEMBER_TOKENS) {
            return usage("GROUP ADD;Id;Correo");
        }
        park.addMember(t[1], t[2]);
        return "OK: User " + t[2] + " added to group " + t[1];
    }

    private String groupRemove(String[] t) {
        if (t.length != GROUP_MEMBER_TOKENS) {
            return usage("GROUP REMOVE;Id;Correo");
        }
        park.removeMember(t[1], t[2]);
        return "OK: User " + t[2] + " removed from group " + t[1];
    }

    private String groupDelete(String[] t) {
        if (t.length != GROUP_DELETE_TOKENS) {
            return usage("GROUP DELETE;Id");
        }
        park.deleteGroup(t[1]);
        return "OK: Group " + t[1] + " deleted";
    }

    private String ticketCreate(String[] t) {
        if (t.length != TICKET_CREATE_TOKENS) {
            return usage("TICKET CREATE;Id;Correo;yyyy-MM-dd");
        }
        Ticket ticket = park.createTicket(t[1], t[2], parseDate(t[3]));
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String ticketDelete(String[] t) {
        if (t.length != TICKET_DELETE_TOKENS) {
            return usage("TICKET DELETE;Id");
        }
        park.deleteTicket(t[1]);
        return "OK: Ticket " + t[1] + " deleted";
    }

    private String groupTicketCreate(String[] t) {
        if (t.length != TICKET_CREATE_TOKENS) {
            return usage("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
        }
        GroupTicket ticket = park.createGroupTicket(t[1], t[2], parseDate(t[3]));
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String groupTicketDelete(String[] t) {
        if (t.length != TICKET_DELETE_TOKENS) {
            return usage("GROUPTICKET DELETE;Id");
        }
        park.deleteGroupTicket(t[1]);
        return "OK: Group ticket " + t[1] + " deleted";
    }

    private String attractionCreate(String[] t) {
        if (t.length != ATTRACTION_CREATE_TOKENS) {
            return usage("ATTRACTION CREATE;Id;Nombre;Capacidad");
        }
        attractions.create(t[1], t[2], t[3]);
        return "OK: Attraction " + t[1] + " created";
    }

    private String attractionEnter(String[] t) {
        if (t.length != ATTRACTION_ACCESS_TOKENS) {
            return usage("ATTRACTION ENTER;IdAtraccion;IdTicket");
        }
        ParkTicket ticket = park.enter(t[1], t[2]);
        Attraction a = attractions.get(t[1]);
        return "OK: " + ticket.getVisitors().size() + " user(s) entered attraction " + a.getId()
                + " (" + a.getAvailable() + " places available)";
    }

    /** Exit by ticket identifier or, as a fallback, by the email of a present user. */
    private String attractionExit(String[] t) {
        if (t.length != ATTRACTION_ACCESS_TOKENS) {
            return usage("ATTRACTION EXIT;IdAtraccion;IdTicket");
        }
        Attraction a = park.exit(t[1], t[2]);
        return "OK: User(s) left attraction " + a.getId() + " (" + a.getAvailable() + " places available)";
    }

    private String attractionStatus(String[] t) {
        if (t.length != ATTRACTION_STATUS_TOKENS) {
            return usage("ATTRACTION STATUS;IdAtraccion");
        }
        Attraction a = attractions.get(t[1]);
        String people = a.getPresent().stream()
                .map(u -> u.getName() + " " + u.getSurname() + " <" + u.getEmail() + ">")
                .reduce((x, y) -> x + ", " + y).orElse("-");
        String nl = System.lineSeparator();
        return "Attraction " + a.getId() + " - " + a.getName() + nl
                + "Capacity: " + a.getCapacity() + nl
                + "Users present: " + a.getPresent().size() + nl
                + "Available places: " + a.getAvailable() + nl
                + "Present: " + people;
    }

    private String systemDate(String[] t) {
        if (t.length != DATE_TOKENS) {
            return usage("SYSTEM DATE;yyyy-MM-dd");
        }
        park.setCurrentDate(parseDate(t[1]));
        return "OK: System date set to " + park.getCurrentDate();
    }

    private String systemPrice(String[] t) {
        if (t.length != PRICE_TOKENS) {
            return usage("SYSTEM PRICE;Precio");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(t[1]);
        } catch (NumberFormatException e) {
            return error("Invalid price " + t[1]);
        }
        park.setCurrentPrice(price, t[1]);
        return "OK: Ticket price set to " + park.getCurrentPrice().toPlainString();
    }

    /** Parses a strict {@code yyyy-MM-dd} date. */
    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date " + text + ". Expected format yyyy-MM-dd", e);
        }
    }

    private static String help() {
        return "Available commands:" + System.lineSeparator() + "  "
                + String.join(System.lineSeparator() + "  ", COMMAND_SYNTAX);
    }

    private String exit() {
        exitRequested = true;
        return "OK: Bye";
    }

    private static String usage(String format) {
        return error("Wrong number of parameters. Usage: " + format);
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
