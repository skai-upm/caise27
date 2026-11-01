package es.upm.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.OperationException;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import es.upm.amusementpark.business.invoice.StandardInvoiceBuilder;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.UserField;

/**
 * Interprets and executes text commands. Commands are split on ';' into the
 * operation keyword and its parameters. Interactive and file input both go
 * through {@link #run(BufferedReader, PrintStream)}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  SYSTEM DATE;yyyy-MM-dd",
            "  SYSTEM PRICE;Precio",
            "  GROUP CREATE;IdGrupo;Nombre",
            "  GROUP ADD;IdGrupo;Correo",
            "  GROUP REMOVE;IdGrupo;Correo",
            "  GROUP DELETE;IdGrupo",
            "  TICKET CREATE;IdTicket;Correo;yyyy-MM-dd",
            "  TICKET DELETE;IdTicket",
            "  GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
            "  GROUPTICKET DELETE;IdTicket",
            "  ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
            "  ATTRACTION ENTER;IdAtraccion;IdTicket",
            "  ATTRACTION EXIT;IdAtraccion;IdTicket",
            "  ATTRACTION STATUS;IdAtraccion",
            "  INVOICE CREATE;USER;Correo",
            "  INVOICE CREATE;GROUP;IdGrupo",
            "  HELP",
            "  EXIT");

    private static final String USER = "user";
    private static final String GROUP = "group";
    private static final String CREATED = "created";
    private static final String DELETED = "deleted";

    private final Park park;
    private final InvoiceController invoices;

    public CommandProcessor(Park park) {
        this.park = park;
        this.invoices = new InvoiceController(park, StandardInvoiceBuilder::new);
    }

    /**
     * Reads commands line by line, executing each nonblank one and printing its
     * result, until EXIT is processed or the input ends.
     */
    public void run(BufferedReader input, PrintStream out) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            CommandResult result = execute(line);
            out.println(result.output());
            if (result.exit()) {
                return;
            }
        }
    }

    /** Executes one command line. */
    public CommandResult execute(String line) {
        String[] parts = line.trim().split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            return switch (parts[0]) {
                case "USER CREATE" -> CommandResult.message(createUser(parts));
                case "USER DELETE" -> CommandResult.message(deleteUser(parts));
                case "USER UPDATE" -> CommandResult.message(updateUser(parts));
                case "SYSTEM DATE" -> CommandResult.message(setDate(parts));
                case "SYSTEM PRICE" -> CommandResult.message(setPrice(parts));
                case "GROUP CREATE" -> CommandResult.message(createGroup(parts));
                case "GROUP ADD" -> CommandResult.message(addToGroup(parts));
                case "GROUP REMOVE" -> CommandResult.message(removeFromGroup(parts));
                case "GROUP DELETE" -> CommandResult.message(deleteGroup(parts));
                case "TICKET CREATE" -> CommandResult.message(createTicket(parts));
                case "TICKET DELETE" -> CommandResult.message(deleteTicket(parts));
                case "GROUPTICKET CREATE" -> CommandResult.message(createGroupTicket(parts));
                case "GROUPTICKET DELETE" -> CommandResult.message(deleteGroupTicket(parts));
                case "ATTRACTION CREATE" -> CommandResult.message(createAttraction(parts));
                case "ATTRACTION ENTER" -> CommandResult.message(enterAttraction(parts));
                case "ATTRACTION EXIT" -> CommandResult.message(exitAttraction(parts));
                case "ATTRACTION STATUS" -> CommandResult.message(attractionStatus(parts));
                case "INVOICE CREATE" -> CommandResult.message(createInvoice(parts));
                case "HELP" -> CommandResult.message(HELP_TEXT);
                case "EXIT" -> new CommandResult("OK: exiting", true);
                default -> CommandResult.message(
                        "ERROR: unknown command '" + parts[0] + "'. Type HELP to list the commands");
            };
        } catch (OperationException e) {
            return CommandResult.message("ERROR: " + e.getMessage());
        }
    }

    private String createUser(String[] parts) {
        requireParameters(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        park.getUserManager().create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return ok(USER, parts[4], CREATED);
    }

    private String deleteUser(String[] parts) {
        requireParameters(parts, 2, "USER DELETE;Correo;Contraseña");
        park.deleteUser(parts[1], parts[2]);
        return ok(USER, parts[1], DELETED);
    }

    private String updateUser(String[] parts) {
        requireParameters(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = UserField.fromName(parts[3]);
        if (field == null) {
            throw new OperationException("field '" + parts[3]
                    + "' cannot be updated; use NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD");
        }
        park.getUserManager().update(parts[1], parts[2], field, parts[4]);
        return ok(USER, parts[1], "updated");
    }

    private String setDate(String[] parts) {
        requireParameters(parts, 1, "SYSTEM DATE;yyyy-MM-dd");
        park.setCurrentDate(parseDate(parts[1]));
        return "OK: system date set to " + park.getCurrentDate();
    }

    private String setPrice(String[] parts) {
        requireParameters(parts, 1, "SYSTEM PRICE;Precio");
        park.setBasePrice(parsePrice(parts[1]));
        return "OK: base price set to " + formatPrice(park.getBasePrice());
    }

    private String createGroup(String[] parts) {
        requireParameters(parts, 2, "GROUP CREATE;IdGrupo;Nombre");
        park.createGroup(parts[1], parts[2]);
        return ok(GROUP, parts[1], CREATED);
    }

    private String addToGroup(String[] parts) {
        requireParameters(parts, 2, "GROUP ADD;IdGrupo;Correo");
        park.addMember(parts[1], parts[2]);
        return ok(USER, parts[2], "added to group " + parts[1]);
    }

    private String removeFromGroup(String[] parts) {
        requireParameters(parts, 2, "GROUP REMOVE;IdGrupo;Correo");
        park.removeMember(parts[1], parts[2]);
        return ok(USER, parts[2], "removed from group " + parts[1]);
    }

    private String deleteGroup(String[] parts) {
        requireParameters(parts, 1, "GROUP DELETE;IdGrupo");
        park.deleteGroup(parts[1]);
        return ok(GROUP, parts[1], DELETED);
    }

    private String createTicket(String[] parts) {
        requireParameters(parts, 3, "TICKET CREATE;IdTicket;Correo;yyyy-MM-dd");
        Ticket ticket = park.createTicket(parts[1], parts[2], parseDate(parts[3]));
        return "OK TICKET CREATED -> " + formatPrice(ticket.getPrice());
    }

    private String deleteTicket(String[] parts) {
        requireParameters(parts, 1, "TICKET DELETE;IdTicket");
        park.deleteTicket(parts[1]);
        return ok("ticket", parts[1], DELETED);
    }

    private String createGroupTicket(String[] parts) {
        requireParameters(parts, 3, "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd");
        Ticket ticket = park.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
        return "OK GROUP TICKET CREATED -> " + formatPrice(ticket.getPrice());
    }

    private String deleteGroupTicket(String[] parts) {
        requireParameters(parts, 1, "GROUPTICKET DELETE;IdTicket");
        park.deleteGroupTicket(parts[1]);
        return ok("group ticket", parts[1], DELETED);
    }

    private String createAttraction(String[] parts) {
        requireParameters(parts, 3, "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad");
        int capacity;
        try {
            capacity = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            throw new OperationException("capacity must be a positive integer");
        }
        park.createAttraction(parts[1], parts[2], capacity);
        return ok("attraction", parts[1], CREATED);
    }

    private String enterAttraction(String[] parts) {
        requireParameters(parts, 2, "ATTRACTION ENTER;IdAtraccion;IdTicket");
        int entered = park.enterAttraction(parts[1], parts[2]);
        return "OK: " + entered + " user(s) entered attraction " + parts[1];
    }

    private String exitAttraction(String[] parts) {
        requireParameters(parts, 2, "ATTRACTION EXIT;IdAtraccion;IdTicket");
        int left = park.exitAttraction(parts[1], parts[2]);
        return "OK: " + left + " user(s) left attraction " + parts[1];
    }

    private String attractionStatus(String[] parts) {
        requireParameters(parts, 1, "ATTRACTION STATUS;IdAtraccion");
        Attraction attraction = park.findAttraction(parts[1]);
        if (attraction == null) {
            throw new OperationException("attraction " + parts[1] + " does not exist");
        }
        List<String> visitors = attraction.getVisitors().stream()
                .map(user -> user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")")
                .toList();
        return String.join(System.lineSeparator(),
                "Attraction: " + attraction.getId() + " - " + attraction.getName(),
                "Maximum capacity: " + attraction.getCapacity(),
                "Users present: " + attraction.getVisitors().size(),
                "Available places: " + attraction.getAvailablePlaces(),
                "Present: " + (visitors.isEmpty() ? "none" : String.join(", ", visitors)));
    }

    private String createInvoice(String[] parts) {
        requireParameters(parts, 2, "INVOICE CREATE;USER;Correo | INVOICE CREATE;GROUP;IdGrupo");
        Invoice invoice = switch (parts[1]) {
            case "USER" -> invoices.userInvoice(parts[2]);
            case "GROUP" -> invoices.groupInvoice(parts[2]);
            default -> throw new OperationException("invoice recipient must be USER or GROUP");
        };
        List<String> lines = new ArrayList<>();
        lines.add("INVOICE - " + invoice.getRecipient());
        for (InvoiceSection section : invoice.getSections()) {
            lines.add(section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                lines.add("  " + line.description() + " " + formatPrice(line.amount()));
            }
        }
        lines.add("TOTAL: " + formatPrice(invoice.getTotal()));
        return String.join(System.lineSeparator(), lines);
    }

    private static String ok(String subject, String id, String outcome) {
        return "OK: " + subject + " " + id + " " + outcome;
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new OperationException("invalid date '" + text + "'; expected yyyy-MM-dd");
        }
    }

    private static BigDecimal parsePrice(String text) {
        int dots = 0;
        for (char c : text.toCharArray()) {
            if (c == '.') {
                dots++;
            } else if (c < '0' || c > '9') {
                throw new OperationException("invalid price '" + text + "'");
            }
        }
        if (dots > 1 || text.startsWith(".") || text.endsWith(".")) {
            throw new OperationException("invalid price '" + text + "'");
        }
        return new BigDecimal(text);
    }

    static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static void requireParameters(String[] parts, int count, String syntax) {
        if (parts.length != count + 1) {
            throw new OperationException("expected " + count + " parameters. Usage: " + syntax);
        }
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new OperationException("parameter " + i + " is empty. Usage: " + syntax);
            }
        }
    }
}
