package es.upm.park.presentation;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.business.service.ParkController;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Presentation of the text commands: splits each line of the form
 * {@code OPERATION;param1;param2;...} with {@link String#split(String, int)}, converts
 * the parameters, delegates to the {@link ParkController} and formats one reply.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    /** Syntax of every available command, shown by HELP. Extend when adding commands. */
    static final List<String> USAGE = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;IdTicket",
            "ATTRACTION CREATE;Id;Nombre;Capacidad",
            "ATTRACTION ENTER;IdAtraccion;IdTicket",
            "ATTRACTION EXIT;IdAtraccion;IdTicket",
            "ATTRACTION STATUS;IdAtraccion",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private static final int PRICE_DECIMALS = 2;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ParkController park;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return "ERROR: empty command";
        }
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> userCreate(parts);
                case "USER DELETE" -> userDelete(parts);
                case "USER UPDATE" -> userUpdate(parts);
                case "GROUP CREATE" -> groupCreate(parts);
                case "GROUP ADD" -> groupAdd(parts);
                case "GROUP REMOVE" -> groupRemove(parts);
                case "GROUP DELETE" -> groupDelete(parts);
                case "TICKET CREATE" -> ticketCreate(parts);
                case "TICKET DELETE" -> ticketDelete(parts);
                case "GROUPTICKET CREATE" -> groupTicketCreate(parts);
                case "GROUPTICKET DELETE" -> groupTicketDelete(parts);
                case "ATTRACTION CREATE" -> attractionCreate(parts);
                case "ATTRACTION ENTER" -> attractionEnter(parts);
                case "ATTRACTION EXIT" -> attractionExit(parts);
                case "ATTRACTION STATUS" -> attractionStatus(parts);
                case "SYSTEM DATE" -> systemDate(parts);
                case "SYSTEM PRICE" -> systemPrice(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR: unknown command '" + operation + "'";
            };
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: command could not be completed (" + e.getMessage() + ")";
        }
    }

    /** True once EXIT has been processed; input loops must stop reading. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    // --- users

    private String userCreate(String[] p) {
        requireParams(p, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "OK: user " + p[4] + " created";
    }

    private String userDelete(String[] p) {
        requireParams(p, 2, "USER DELETE;Correo;Contraseña");
        park.deleteUser(p[1], p[2]);
        return "OK: user " + p[1] + " deleted";
    }

    private String userUpdate(String[] p) {
        requireParams(p, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = parseField(p[3]);
        park.updateUser(p[1], p[2], field, p[4]);
        return "OK: user " + p[1] + " updated (" + field + ")";
    }

    // --- groups

    private String groupCreate(String[] p) {
        requireParams(p, 2, "GROUP CREATE;Id;Nombre");
        park.createGroup(p[1], p[2]);
        return "OK: group " + p[1] + " created";
    }

    private String groupAdd(String[] p) {
        requireParams(p, 2, "GROUP ADD;Id;Correo");
        park.addGroupMember(p[1], p[2]);
        return "OK: user " + p[2] + " added to group " + p[1];
    }

    private String groupRemove(String[] p) {
        requireParams(p, 2, "GROUP REMOVE;Id;Correo");
        park.removeGroupMember(p[1], p[2]);
        return "OK: user " + p[2] + " removed from group " + p[1];
    }

    private String groupDelete(String[] p) {
        requireParams(p, 1, "GROUP DELETE;Id");
        park.deleteGroup(p[1]);
        return "OK: group " + p[1] + " deleted";
    }

    // --- tickets

    private String ticketCreate(String[] p) {
        requireParams(p, 3, "TICKET CREATE;Id;Correo;yyyy-MM-dd");
        return "OK TICKET CREATED -> " + park.createTicket(p[1], p[2], parseDate(p[3])).getPrice().toPlainString();
    }

    private String ticketDelete(String[] p) {
        requireParams(p, 1, "TICKET DELETE;Id");
        park.deleteTicket(p[1]);
        return "OK: ticket " + p[1] + " deleted";
    }

    private String groupTicketCreate(String[] p) {
        requireParams(p, 3, "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd");
        return "OK GROUP TICKET CREATED -> "
                + park.createGroupTicket(p[1], p[2], parseDate(p[3])).getPrice().toPlainString();
    }

    private String groupTicketDelete(String[] p) {
        requireParams(p, 1, "GROUPTICKET DELETE;IdTicket");
        park.deleteGroupTicket(p[1]);
        return "OK: group ticket " + p[1] + " deleted";
    }

    // --- attractions

    private String attractionCreate(String[] p) {
        requireParams(p, 3, "ATTRACTION CREATE;Id;Nombre;Capacidad");
        park.createAttraction(p[1], p[2], parseCapacity(p[3]));
        return "OK: attraction " + p[1] + " created";
    }

    private String attractionEnter(String[] p) {
        requireParams(p, 2, "ATTRACTION ENTER;IdAtraccion;IdTicket");
        Attraction attraction = park.enterAttraction(p[1], p[2]);
        int entered = park.findTicket(p[2]).getHolders().size();
        return "OK: " + entered + " user(s) entered attraction " + p[1]
                + " (" + attraction.getAvailablePlaces() + " places available)";
    }

    private String attractionExit(String[] p) {
        requireParams(p, 2, "ATTRACTION EXIT;IdAtraccion;IdTicket");
        int left = park.exitAttraction(p[1], p[2]);
        return "OK: " + left + " user(s) left attraction " + p[1]
                + " (" + park.findAttraction(p[1]).getAvailablePlaces() + " places available)";
    }

    private String attractionStatus(String[] p) {
        requireParams(p, 1, "ATTRACTION STATUS;IdAtraccion");
        Attraction a = park.findAttraction(p[1]);
        String nl = System.lineSeparator();
        StringBuilder present = new StringBuilder();
        for (User u : a.getPresentUsers()) {
            if (!present.isEmpty()) {
                present.append(", ");
            }
            present.append(u.getName()).append(' ').append(u.getSurnames()).append(" <").append(u.getEmail()).append('>');
        }
        return "OK: attraction " + a.getId() + " - " + a.getName() + nl
                + "  capacity: " + a.getCapacity() + nl
                + "  present: " + a.getPresentCount() + nl
                + "  available: " + a.getAvailablePlaces() + nl
                + "  users: " + (present.isEmpty() ? "(none)" : present);
    }

    // --- system and general commands

    private String systemDate(String[] p) {
        requireParams(p, 1, "SYSTEM DATE;yyyy-MM-dd");
        park.setCurrentDate(parseDate(p[1]));
        return "OK: system date set to " + park.getCurrentDate();
    }

    private String systemPrice(String[] p) {
        requireParams(p, 1, "SYSTEM PRICE;Precio");
        park.setCurrentPrice(parsePrice(p[1]));
        return "OK: ticket price set to " + park.getCurrentPrice().toPlainString();
    }

    private String help(String[] p) {
        requireParams(p, 0, "HELP");
        StringBuilder sb = new StringBuilder("OK: available commands:");
        for (String usage : USAGE) {
            sb.append(System.lineSeparator()).append("  ").append(usage);
        }
        sb.append(System.lineSeparator()).append("  Campo (USER UPDATE): ")
                .append(String.join(", ", Arrays.stream(UserField.values()).map(Enum::name).toList()));
        return sb.toString();
    }

    private String exit(String[] p) {
        requireParams(p, 0, "EXIT");
        exitRequested = true;
        return "OK: application terminated";
    }

    // --- parameter conversion

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date '" + value + "', expected format yyyy-MM-dd");
        }
    }

    /** Positive decimal amount: digits, optionally '.' and one or two decimals (no regex). */
    static BigDecimal parsePrice(String value) {
        String error = "invalid price '" + value + "', expected a positive amount such as 60.00";
        int dot = value.indexOf('.');
        String integer = dot < 0 ? value : value.substring(0, dot);
        String decimals = dot < 0 ? "" : value.substring(dot + 1);
        boolean wellFormed = !integer.isEmpty() && isDigits(integer) && isDigits(decimals)
                && (dot < 0 || (!decimals.isEmpty() && decimals.length() <= PRICE_DECIMALS));
        if (!wellFormed) {
            throw new IllegalArgumentException(error);
        }
        BigDecimal price = new BigDecimal(value).setScale(PRICE_DECIMALS, RoundingMode.UNNECESSARY);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException(error);
        }
        return price;
    }

    /** Whole number written with digits only. */
    static int parseCapacity(String text) {
        String error = "invalid capacity '" + text + "', expected a positive whole number";
        if (text.isEmpty() || !isDigits(text)) {
            throw new IllegalArgumentException(error);
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private static boolean isDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static UserField parseField(String name) {
        try {
            return UserField.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unsupported field '" + name + "'");
        }
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("expected " + expected + " parameters, usage: " + usage);
        }
    }
}
