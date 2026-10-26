package es.upm.parque.presentation;

import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.InvoiceSection;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
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
import java.util.Arrays;
import java.util.List;

/**
 * Interprets text commands such as {@code USER CREATE;Ana;...}. The command is split with
 * {@link String#split} into the operation (first segment) and its parameters (remaining segments).
 * Every command produces a response; no command can make the application stop unexpectedly.
 * The same processor serves interactive and file input through {@link #run}.
 * It only deals with text: parsing, parameter checks and responses. Every operation is delegated to the
 * business {@link ParkController}.
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";

    private static final String USAGE_USER_CREATE = "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta";
    private static final String USAGE_USER_DELETE = "USER DELETE;Correo;Contraseña";
    private static final String USAGE_USER_UPDATE = "USER UPDATE;Correo;Contraseña;Campo;NuevoValor";
    private static final String USAGE_GROUP_CREATE = "GROUP CREATE;Id;Nombre";
    private static final String USAGE_GROUP_ADD = "GROUP ADD;Id;Correo";
    private static final String USAGE_GROUP_REMOVE = "GROUP REMOVE;Id;Correo";
    private static final String USAGE_GROUP_DELETE = "GROUP DELETE;Id";
    private static final String USAGE_TICKET_CREATE = "TICKET CREATE;Id;Correo;yyyy-MM-dd";
    private static final String USAGE_TICKET_DELETE = "TICKET DELETE;Id";
    private static final String USAGE_GROUPTICKET_CREATE = "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd";
    private static final String USAGE_GROUPTICKET_DELETE = "GROUPTICKET DELETE;Id";
    private static final String USAGE_ATTRACTION_CREATE = "ATTRACTION CREATE;Id;Nombre;Capacidad";
    private static final String USAGE_ATTRACTION_DELETE = "ATTRACTION DELETE;Id";
    private static final String USAGE_ATTRACTION_ENTER = "ATTRACTION ENTER;Id;IdTicket";
    private static final String USAGE_ATTRACTION_EXIT = "ATTRACTION EXIT;Id;IdTicket";
    private static final String USAGE_ATTRACTION_STATUS = "ATTRACTION STATUS;Id";
    private static final String USAGE_INVOICE_USER = "INVOICE CREATE;USER;Correo";
    private static final String USAGE_INVOICE_GROUP = "INVOICE CREATE;GROUP;IdGrupo";
    private static final String USAGE_SYSTEM_DATE = "SYSTEM DATE;yyyy-MM-dd";
    private static final String USAGE_SYSTEM_PRICE = "SYSTEM PRICE;Importe";
    private static final String USAGE_HELP = "HELP";
    private static final String USAGE_EXIT = "EXIT";

    private static final List<String> HELP_LINES = List.of(
        USAGE_USER_CREATE,
        USAGE_USER_DELETE,
        USAGE_USER_UPDATE + "  (Campo: " + Arrays.toString(UserField.values()) + ")",
        USAGE_GROUP_CREATE,
        USAGE_GROUP_ADD,
        USAGE_GROUP_REMOVE,
        USAGE_GROUP_DELETE,
        USAGE_TICKET_CREATE,
        USAGE_TICKET_DELETE,
        USAGE_GROUPTICKET_CREATE,
        USAGE_GROUPTICKET_DELETE,
        USAGE_ATTRACTION_CREATE,
        USAGE_ATTRACTION_DELETE,
        USAGE_ATTRACTION_ENTER,
        USAGE_ATTRACTION_EXIT,
        USAGE_ATTRACTION_STATUS,
        USAGE_INVOICE_USER,
        USAGE_INVOICE_GROUP,
        USAGE_SYSTEM_DATE,
        USAGE_SYSTEM_PRICE,
        USAGE_HELP,
        USAGE_EXIT);

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ParkController park;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Reads commands line by line and prints each response until EXIT is executed or the input ends.
     */
    public void run(BufferedReader in, PrintStream out) throws IOException {
        String line;
        while (!exitRequested && (line = in.readLine()) != null) {
            String response = execute(line);
            if (response != null) {
                out.println(response);
            }
        }
    }

    /**
     * Executes one command line.
     *
     * @return the response to show, or {@code null} for a blank line
     */
    public String execute(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.trim().split(PARAM_SEPARATOR, -1);
        String[] operation = parts[0].trim().split(" ");
        try {
            if (operation.length == 1) {
                return executeGeneral(operation[0], parts);
            }
            if (operation.length == 2 && operation[0].equals("USER")) {
                return executeUser(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("GROUP")) {
                return executeGroup(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("TICKET")) {
                return executeTicket(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("GROUPTICKET")) {
                return executeGroupTicket(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("ATTRACTION")) {
                return executeAttraction(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("INVOICE")) {
                return executeInvoice(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM")) {
                return executeSystem(operation[1], parts);
            }
            return error("Unknown command: " + parts[0].trim());
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected problem: " + e.getMessage());
        }
    }

    private String executeGeneral(String operation, String[] parts) {
        switch (operation) {
            case "HELP" -> {
                requireParams(parts, 0, USAGE_HELP);
                return "Available commands:" + System.lineSeparator() + "  "
                    + String.join(System.lineSeparator() + "  ", HELP_LINES);
            }
            case "EXIT" -> {
                requireParams(parts, 0, USAGE_EXIT);
                exitRequested = true;
                return ok("Bye");
            }
            default -> {
                return error("Unknown command: " + operation);
            }
        }
    }

    private String executeUser(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 7, USAGE_USER_CREATE);
                User u = park.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("User " + u.getDni() + " created");
            }
            case "DELETE" -> {
                requireParams(parts, 2, USAGE_USER_DELETE);
                park.deleteUser(parts[1], parts[2]);
                return ok("User " + parts[1] + " deleted");
            }
            case "UPDATE" -> {
                requireParams(parts, 4, USAGE_USER_UPDATE);
                UserField field = parseField(parts[3]);
                park.updateUser(parts[1], parts[2], field, parts[4]);
                return ok("User " + parts[1] + " updated (" + field + ")");
            }
            default -> {
                return error("Unknown command: USER " + action);
            }
        }
    }

    private String executeGroup(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 2, USAGE_GROUP_CREATE);
                park.createGroup(parts[1], parts[2]);
                return ok("Group " + parts[1] + " created");
            }
            case "ADD" -> {
                requireParams(parts, 2, USAGE_GROUP_ADD);
                park.addGroupMember(parts[1], parts[2]);
                return ok("User " + parts[2] + " added to group " + parts[1]);
            }
            case "REMOVE" -> {
                requireParams(parts, 2, USAGE_GROUP_REMOVE);
                park.removeGroupMember(parts[1], parts[2]);
                return ok("User " + parts[2] + " removed from group " + parts[1]);
            }
            case "DELETE" -> {
                requireParams(parts, 1, USAGE_GROUP_DELETE);
                park.deleteGroup(parts[1]);
                return ok("Group " + parts[1] + " deleted");
            }
            default -> {
                return error("Unknown command: GROUP " + action);
            }
        }
    }

    private String executeTicket(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 3, USAGE_TICKET_CREATE);
                Ticket t = park.createTicket(parts[1], parts[2], parseDate(parts[3]));
                return "OK TICKET CREATED -> " + formatPrice(t.getPrice());
            }
            case "DELETE" -> {
                requireParams(parts, 1, USAGE_TICKET_DELETE);
                park.deleteTicket(parts[1]);
                return ok("Ticket " + parts[1] + " deleted");
            }
            default -> {
                return error("Unknown command: TICKET " + action);
            }
        }
    }

    private String executeGroupTicket(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 3, USAGE_GROUPTICKET_CREATE);
                Ticket t = park.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
                return "OK GROUP TICKET CREATED -> " + formatPrice(t.getPrice());
            }
            case "DELETE" -> {
                requireParams(parts, 1, USAGE_GROUPTICKET_DELETE);
                park.deleteGroupTicket(parts[1]);
                return ok("Group ticket " + parts[1] + " deleted");
            }
            default -> {
                return error("Unknown command: GROUPTICKET " + action);
            }
        }
    }

    private String executeAttraction(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 3, USAGE_ATTRACTION_CREATE);
                Attraction a = park.createAttraction(parts[1], parts[2], parts[3]);
                return ok("Attraction " + a.getId() + " created with capacity " + a.getCapacity());
            }
            case "DELETE" -> {
                requireParams(parts, 1, USAGE_ATTRACTION_DELETE);
                park.deleteAttraction(parts[1]);
                return ok("Attraction " + parts[1] + " deleted");
            }
            case "ENTER" -> {
                requireParams(parts, 2, USAGE_ATTRACTION_ENTER);
                int entered = park.enterAttraction(parts[1], parts[2]);
                return ok(entered + " user(s) entered attraction " + parts[1]);
            }
            case "EXIT" -> {
                requireParams(parts, 2, USAGE_ATTRACTION_EXIT);
                int left = park.exitAttraction(parts[1], parts[2]);
                return ok(left + " user(s) left attraction " + parts[1]);
            }
            case "STATUS" -> {
                requireParams(parts, 1, USAGE_ATTRACTION_STATUS);
                return formatStatus(park.getAttraction(parts[1]));
            }
            default -> {
                return error("Unknown command: ATTRACTION " + action);
            }
        }
    }

    private static String formatStatus(Attraction a) {
        List<String> names = new ArrayList<>();
        for (User u : a.getPresent()) {
            names.add(u.getName() + " " + u.getSurname() + " <" + u.getEmail() + ">");
        }
        String nl = System.lineSeparator();
        return "Attraction " + a.getId() + " - " + a.getName() + nl
            + "  Capacity: " + a.getCapacity() + " | Present: " + a.getPresent().size()
            + " | Available: " + a.getAvailable() + nl
            + "  Users present: " + (names.isEmpty() ? "(none)" : String.join(", ", names));
    }

    private String executeInvoice(String action, String[] parts) {
        if (!action.equals("CREATE")) {
            return error("Unknown command: INVOICE " + action);
        }
        requireParams(parts, 2, USAGE_INVOICE_USER + " | " + USAGE_INVOICE_GROUP);
        return switch (parts[1].trim().toUpperCase()) {
            case "USER" -> formatInvoice(park.createUserInvoice(parts[2]));
            case "GROUP" -> formatInvoice(park.createGroupInvoice(parts[2]));
            default -> error("Unknown invoice recipient type " + parts[1] + ". Expected USER or GROUP");
        };
    }

    private static String formatInvoice(Invoice invoice) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("INVOICE - ").append(invoice.getRecipient()).append(nl)
            .append("  Date: ").append(invoice.getIssueDate()).append(nl);
        for (InvoiceSection section : invoice.getSections()) {
            text.append("  ").append(section.getTitle()).append(nl);
            for (InvoiceLine line : section.getLines()) {
                text.append("    ").append(line.getDescription()).append(": ").append(formatPrice(line.getAmount()))
                    .append(nl);
            }
            text.append("    Subtotal: ").append(formatPrice(section.getSubtotal())).append(nl);
        }
        if (invoice.getSections().isEmpty()) {
            text.append("  (no items)").append(nl);
        }
        return text.append("  TOTAL: ").append(formatPrice(invoice.getTotal())).toString();
    }

    private String executeSystem(String action, String[] parts) {
        switch (action) {
            case "DATE" -> {
                requireParams(parts, 1, USAGE_SYSTEM_DATE);
                park.setCurrentDate(parseDate(parts[1]));
                return ok("System date set to " + park.getCurrentDate());
            }
            case "PRICE" -> {
                requireParams(parts, 1, USAGE_SYSTEM_PRICE);
                park.setCurrentPrice(parsePrice(parts[1]));
                return ok("Ticket price set to " + formatPrice(park.getCurrentPrice()));
            }
            default -> {
                return error("Unknown command: SYSTEM " + action);
            }
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date " + text + ". Expected format yyyy-MM-dd");
        }
    }

    private static BigDecimal parsePrice(String text) {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid price " + text + ". Expected a positive amount with at most two decimals");
        }
    }

    private static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static UserField parseField(String text) {
        try {
            return UserField.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Field " + text + " cannot be updated. Editable fields: "
                + Arrays.toString(UserField.values()));
        }
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        int given = parts.length - 1;
        if (given != expected) {
            throw new IllegalArgumentException(
                "Expected " + expected + " parameters but got " + given + ". Usage: " + usage);
        }
    }

    private static String ok(String message) {
        return "OK: " + message;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
