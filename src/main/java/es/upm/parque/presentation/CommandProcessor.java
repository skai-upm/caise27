package es.upm.parque.presentation;

import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.InvoiceSection;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpeedPass;
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
import java.util.Map;

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

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Help mode selected by plain {@code HELP}. */
    public static final String DEFAULT_HELP_MODE = "";
    /** Help mode selected by {@code HELP EXTENDED}. */
    public static final String EXTENDED_HELP_MODE = "EXTENDED";

    private final ParkController park;
    private final Map<String, HelpGenerator> helpGenerators;
    private boolean exitRequested;

    /**
     * @param helpGenerators help generator for each help mode ({@link #DEFAULT_HELP_MODE} for plain HELP)
     */
    public CommandProcessor(ParkController park, Map<String, HelpGenerator> helpGenerators) {
        this.park = park;
        this.helpGenerators = Map.copyOf(helpGenerators);
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
            if (operation[0].equals("HELP") && operation.length <= 2) {
                return executeHelp(operation.length == 1 ? DEFAULT_HELP_MODE : operation[1], parts);
            }
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
            if (operation.length == 2 && operation[0].equals("SPEEDPASS")) {
                return executeSpeedPass(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SPECIALPASS")) {
                return executeSpecialPass(operation[1], parts);
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

    private String executeHelp(String mode, String[] parts) {
        HelpGenerator generator = helpGenerators.get(mode);
        if (generator == null) {
            return error("Unknown help mode: " + mode);
        }
        requireParams(parts, 0, DEFAULT_HELP_MODE.equals(mode) ? CommandCatalog.HELP.syntax() : "HELP " + mode);
        return generator.generate();
    }

    private String executeGeneral(String operation, String[] parts) {
        switch (operation) {
            case "EXIT" -> {
                requireParams(parts, 0, CommandCatalog.EXIT.syntax());
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
                requireParams(parts, 7, CommandCatalog.USER_CREATE.syntax());
                User u = park.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("User " + u.getDni() + " created");
            }
            case "DELETE" -> {
                requireParams(parts, 2, CommandCatalog.USER_DELETE.syntax());
                park.deleteUser(parts[1], parts[2]);
                return ok("User " + parts[1] + " deleted");
            }
            case "UPDATE" -> {
                requireParams(parts, 4, CommandCatalog.USER_UPDATE.syntax());
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
                requireParams(parts, 2, CommandCatalog.GROUP_CREATE.syntax());
                park.createGroup(parts[1], parts[2]);
                return ok("Group " + parts[1] + " created");
            }
            case "ADD" -> {
                requireParams(parts, 2, CommandCatalog.GROUP_ADD.syntax());
                park.addGroupMember(parts[1], parts[2]);
                return ok("User " + parts[2] + " added to group " + parts[1]);
            }
            case "REMOVE" -> {
                requireParams(parts, 2, CommandCatalog.GROUP_REMOVE.syntax());
                park.removeGroupMember(parts[1], parts[2]);
                return ok("User " + parts[2] + " removed from group " + parts[1]);
            }
            case "DELETE" -> {
                requireParams(parts, 1, CommandCatalog.GROUP_DELETE.syntax());
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
                requireParams(parts, 3, CommandCatalog.TICKET_CREATE.syntax());
                Ticket t = park.createTicket(parts[1], parts[2], parseDate(parts[3]));
                return "OK TICKET CREATED -> " + formatPrice(t.getPrice());
            }
            case "DELETE" -> {
                requireParams(parts, 1, CommandCatalog.TICKET_DELETE.syntax());
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
                requireParams(parts, 3, CommandCatalog.GROUPTICKET_CREATE.syntax());
                Ticket t = park.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
                return "OK GROUP TICKET CREATED -> " + formatPrice(t.getPrice());
            }
            case "DELETE" -> {
                requireParams(parts, 1, CommandCatalog.GROUPTICKET_DELETE.syntax());
                park.deleteGroupTicket(parts[1]);
                return ok("Group ticket " + parts[1] + " deleted");
            }
            default -> {
                return error("Unknown command: GROUPTICKET " + action);
            }
        }
    }

    private String executeSpeedPass(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 2, CommandCatalog.SPEEDPASS_CREATE.syntax());
                List<SpeedPass> passes = park.buySpeedPasses(parts[1], parseQuantity(parts[2]));
                BigDecimal total = BigDecimal.ZERO;
                for (SpeedPass p : passes) {
                    total = total.add(p.getPrice());
                }
                String ids = passes.size() == 1 ? passes.get(0).getId()
                    : passes.get(0).getId() + ".." + passes.get(passes.size() - 1).getId();
                return "OK SPEEDPASS CREATED -> " + formatPrice(total) + " (" + ids + ")";
            }
            case "DELETE" -> {
                requireParams(parts, 1, CommandCatalog.SPEEDPASS_DELETE.syntax());
                return refunded(park.refundSpeedPass(parts[1]));
            }
            default -> {
                return error("Unknown command: SPEEDPASS " + action);
            }
        }
    }

    private String executeSpecialPass(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 1, CommandCatalog.SPECIALPASS_CREATE.syntax());
                Pass pass = park.buySpecialPass(parts[1]);
                return "OK SPECIALPASS CREATED -> " + formatPrice(pass.getPrice()) + " (" + pass.getId() + ")";
            }
            case "DELETE" -> {
                requireParams(parts, 1, CommandCatalog.SPECIALPASS_DELETE.syntax());
                return refunded(park.refundSpecialPass(parts[1]));
            }
            default -> {
                return error("Unknown command: SPECIALPASS " + action);
            }
        }
    }

    private static String refunded(Pass pass) {
        return ok(pass.getTypeName() + " " + pass.getId() + " refunded -> " + formatPrice(pass.getPrice()));
    }

    private static int parseQuantity(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity " + text + ". Expected 1 or 10");
        }
    }

    private String executeAttraction(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                if (parts.length != 5) {
                    requireParams(parts, 3, CommandCatalog.ATTRACTION_CREATE.syntax() + " | "
                        + CommandCatalog.ATTRACTION_CREATE_TYPED.syntax());
                }
                Attraction<?> a = park.createAttraction(parts[1], parts[2], parts[3], parts.length == 5 ? parts[4] : null);
                return ok("Attraction " + a.getId() + " (" + a.getType() + ") created with capacity " + a.getCapacity());
            }
            case "DELETE" -> {
                requireParams(parts, 1, CommandCatalog.ATTRACTION_DELETE.syntax());
                park.deleteAttraction(parts[1]);
                return ok("Attraction " + parts[1] + " deleted");
            }
            case "ENTER" -> {
                requireParams(parts, 2, CommandCatalog.ATTRACTION_ENTER.syntax());
                int entered = park.enterAttraction(parts[1], parts[2]);
                return ok(entered + " user(s) entered attraction " + parts[1]);
            }
            case "EXIT" -> {
                requireParams(parts, 2, CommandCatalog.ATTRACTION_EXIT.syntax());
                int left = park.exitAttraction(parts[1], parts[2]);
                return ok(left + " user(s) left attraction " + parts[1]);
            }
            case "STATUS" -> {
                requireParams(parts, 1, CommandCatalog.ATTRACTION_STATUS.syntax());
                return formatStatus(park.getAttraction(parts[1]));
            }
            default -> {
                return error("Unknown command: ATTRACTION " + action);
            }
        }
    }

    private static String formatStatus(Attraction<?> a) {
        List<String> names = new ArrayList<>();
        for (User u : a.getPresent()) {
            names.add(u.getName() + " " + u.getSurname() + " <" + u.getEmail() + ">");
        }
        String nl = System.lineSeparator();
        return "Attraction " + a.getId() + " - " + a.getName() + " (" + a.getType() + ")" + nl
            + "  Capacity: " + a.getCapacity() + " | Present: " + a.getPresent().size()
            + " | Available: " + a.getAvailable() + nl
            + "  Users present: " + (names.isEmpty() ? "(none)" : String.join(", ", names));
    }

    private String executeInvoice(String action, String[] parts) {
        if (!action.equals("CREATE")) {
            return error("Unknown command: INVOICE " + action);
        }
        requireParams(parts, 2, CommandCatalog.INVOICE_USER.syntax() + " | " + CommandCatalog.INVOICE_GROUP.syntax());
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
                requireParams(parts, 1, CommandCatalog.SYSTEM_DATE.syntax());
                park.setCurrentDate(parseDate(parts[1]));
                return ok("System date set to " + park.getCurrentDate());
            }
            case "PRICE" -> {
                requireParams(parts, 1, CommandCatalog.SYSTEM_PRICE.syntax());
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
