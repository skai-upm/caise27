package es.upm.parque.presentation;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.presentation.help.HelpGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...} and returns a response line.
 */
public class CommandProcessor {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final AttractionController attractionController;
    private final InvoiceController invoiceController;
    private final PassController passController;
    private final SystemDate systemDate;
    private final Map<String, HelpGenerator> helpGenerators;
    private boolean exitRequested;

    public CommandProcessor(UserController userController, GroupController groupController,
            TicketController ticketController, AttractionController attractionController,
            InvoiceController invoiceController, PassController passController, SystemDate systemDate,
            Map<String, HelpGenerator> helpGenerators) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.attractionController = attractionController;
        this.invoiceController = invoiceController;
        this.passController = passController;
        this.systemDate = systemDate;
        this.helpGenerators = Map.copyOf(helpGenerators);
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        String[] operation = parts[0].trim().split(" ");
        try {
            if (operation.length == 2 && operation[0].equals("USER")) {
                return processUser(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("GROUP")) {
                return processGroup(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("TICKET")) {
                return processTicket(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("GROUPTICKET")) {
                return processGroupTicket(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SPEEDPASS")) {
                return processSpeedPass(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SPECIALPASS")) {
                return processSpecialPass(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("ATTRACTION")) {
                return processAttraction(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("INVOICE") && operation[1].equals("CREATE")) {
                requireParams(parts, 2, "INVOICE CREATE;USER;Correo or INVOICE CREATE;GROUP;IdGrupo");
                return processInvoice(parts[1].trim(), parts[2]);
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM") && operation[1].equals("DATE")) {
                requireParams(parts, 1, "SYSTEM DATE;yyyy-MM-dd");
                systemDate.set(parseDate(parts[1]));
                return "OK SYSTEM DATE -> " + systemDate.get();
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM") && operation[1].equals("PRICE")) {
                requireParams(parts, 1, "SYSTEM PRICE;Importe");
                ticketController.setPrice(parsePrice(parts[1]));
                return "OK SYSTEM PRICE -> " + ticketController.getPrice().toPlainString();
            }
            if (operation[0].equals("HELP") && operation.length <= 2) {
                requireParams(parts, 0, "HELP or HELP EXTENDED");
                return help(operation.length == 1 ? "" : operation[1]);
            }
            if (operation.length == 1 && operation[0].equals("EXIT")) {
                requireParams(parts, 0, "EXIT");
                exitRequested = true;
                return "OK EXIT";
            }
            return "ERROR: unknown command '" + parts[0].trim() + "'";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public SystemDate getSystemDate() {
        return systemDate;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    private String help(String mode) {
        HelpGenerator generator = helpGenerators.get(mode);
        if (generator == null) {
            throw new IllegalArgumentException("unknown help mode '" + mode + "'");
        }
        return generator.generate();
    }

    private String processUser(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                User u = userController.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                return "OK USER CREATED -> " + u;
            }
            case "DELETE" -> {
                requireParams(p, 2, "USER DELETE;Correo;Contraseña");
                User deleted = userController.delete(p[1], p[2]);
                groupController.removeUserFromAllGroups(deleted);
                return "OK USER DELETED -> " + p[1];
            }
            case "UPDATE" -> {
                requireParams(p, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                UserField field = parseField(p[3]);
                User u = userController.update(p[1], p[2], field, p[4]);
                return "OK USER UPDATED -> " + u;
            }
            default -> {
                return "ERROR: unknown command 'USER " + action + "'";
            }
        }
    }

    private String processGroup(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 2, "GROUP CREATE;Id;Nombre");
                return "OK GROUP CREATED -> " + groupController.create(p[1], p[2]);
            }
            case "ADD" -> {
                requireParams(p, 2, "GROUP ADD;Id;Correo");
                return "OK GROUP MEMBER ADDED -> " + groupController.addMember(p[1], p[2]);
            }
            case "REMOVE" -> {
                requireParams(p, 2, "GROUP REMOVE;Id;Correo");
                return "OK GROUP MEMBER REMOVED -> " + groupController.removeMember(p[1], p[2]);
            }
            case "DELETE" -> {
                requireParams(p, 1, "GROUP DELETE;Id");
                return "OK GROUP DELETED -> " + groupController.delete(p[1]).getId();
            }
            default -> {
                return "ERROR: unknown command 'GROUP " + action + "'";
            }
        }
    }

    private String processTicket(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 3, "TICKET CREATE;Id;Correo;yyyy-MM-dd");
                IndividualTicket t = ticketController.create(p[1], p[2], parseDate(p[3]));
                return "OK TICKET CREATED -> " + t.getPrice().toPlainString();
            }
            case "DELETE" -> {
                requireParams(p, 1, "TICKET DELETE;Id");
                return "OK TICKET DELETED -> " + ticketController.delete(p[1]).getId();
            }
            default -> {
                return "ERROR: unknown command 'TICKET " + action + "'";
            }
        }
    }

    private String processGroupTicket(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 3, "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
                GroupTicket t = ticketController.createGroupTicket(p[1], p[2], parseDate(p[3]));
                return "OK GROUP TICKET CREATED -> " + t.getPrice().toPlainString();
            }
            case "DELETE" -> {
                requireParams(p, 1, "GROUPTICKET DELETE;Id");
                return "OK GROUP TICKET DELETED -> " + ticketController.deleteGroupTicket(p[1]).getId();
            }
            default -> {
                return "ERROR: unknown command 'GROUPTICKET " + action + "'";
            }
        }
    }

    private String processAttraction(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                if (p.length != 4 && p.length != 5) {
                    requireParams(p, 4, "ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]");
                }
                AttractionType type = p.length == 5 ? parseAttractionType(p[4]) : AttractionType.GENERAL;
                return "OK ATTRACTION CREATED -> " + attractionController.create(p[1], p[2], parseCapacity(p[3]), type);
            }
            case "DELETE" -> {
                requireParams(p, 1, "ATTRACTION DELETE;Id");
                return "OK ATTRACTION DELETED -> " + attractionController.delete(p[1]).getId();
            }
            case "ENTER" -> {
                if (p.length != 3 && p.length != 4) {
                    requireParams(p, 2, "ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]");
                }
                boolean speed = p.length == 4;
                if (speed && !p[3].trim().equalsIgnoreCase("SPEED")) {
                    throw new IllegalArgumentException("unknown entry option '" + p[3] + "': only SPEED is allowed");
                }
                List<Pass> consumed = attractionController.enter(p[1], p[2], speed);
                return "OK ATTRACTION ENTERED -> " + attractionController.findById(p[1]) + consumedPasses(consumed);
            }
            case "EXIT" -> {
                requireParams(p, 2, "ATTRACTION EXIT;IdAtraccion;IdEntrada");
                return "OK ATTRACTION EXITED -> " + attractionController.exit(p[1], p[2]);
            }
            case "STATUS" -> {
                requireParams(p, 1, "ATTRACTION STATUS;IdAtraccion");
                return "OK " + attractionController.status(p[1]);
            }
            default -> {
                return "ERROR: unknown command 'ATTRACTION " + action + "'";
            }
        }
    }

    private String processSpeedPass(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 2, "SPEEDPASS CREATE;IdEntrada;Cantidad");
                List<SpeedPass> bought = passController.buySpeedPasses(p[1], parseQuantity(p[2]));
                BigDecimal total = BigDecimal.ZERO;
                for (SpeedPass pass : bought) {
                    total = total.add(pass.getPrice());
                }
                String ids = bought.size() == 1 ? bought.get(0).getId()
                        : bought.get(0).getId() + " .. " + bought.get(bought.size() - 1).getId();
                return "OK SPEEDPASS CREATED -> " + bought.size() + " x " + bought.get(0).getPrice().toPlainString()
                        + " = " + total.toPlainString() + " (" + ids + ")";
            }
            case "DELETE" -> {
                requireParams(p, 1, "SPEEDPASS DELETE;IdPase");
                SpeedPass pass = passController.refundSpeedPass(p[1]);
                return "OK SPEEDPASS REFUNDED -> " + pass.getId() + " -> " + pass.getPrice().toPlainString();
            }
            default -> {
                return "ERROR: unknown command 'SPEEDPASS " + action + "'";
            }
        }
    }

    private String processSpecialPass(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 1, "SPECIALPASS CREATE;IdEntrada");
                SpecialPass pass = passController.buySpecialPass(p[1]);
                return "OK SPECIALPASS CREATED -> " + pass.getId() + " -> " + pass.getPrice().toPlainString();
            }
            case "DELETE" -> {
                requireParams(p, 1, "SPECIALPASS DELETE;IdPase");
                SpecialPass pass = passController.refundSpecialPass(p[1]);
                return "OK SPECIALPASS REFUNDED -> " + pass.getId() + " -> " + pass.getPrice().toPlainString();
            }
            default -> {
                return "ERROR: unknown command 'SPECIALPASS " + action + "'";
            }
        }
    }

    private static int parseQuantity(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid quantity '" + text + "': use 1 or 10");
        }
    }

    private String processInvoice(String recipientType, String recipient) {
        Invoice invoice = switch (recipientType) {
            case "USER" -> invoiceController.userInvoice(recipient);
            case "GROUP" -> invoiceController.groupInvoice(recipient);
            default -> throw new IllegalArgumentException(
                    "invalid invoice recipient '" + recipientType + "': use USER or GROUP");
        };
        return "OK INVOICE CREATED" + System.lineSeparator() + invoice.format();
    }

    private static String consumedPasses(List<Pass> consumed) {
        if (consumed.isEmpty()) {
            return "";
        }
        List<String> ids = new ArrayList<>();
        for (Pass pass : consumed) {
            ids.add(pass.getId());
        }
        return " [" + consumed.get(0).getKind() + " consumed: " + String.join(", ", ids) + "]";
    }

    private static AttractionType parseAttractionType(String text) {
        for (AttractionType type : AttractionType.values()) {
            if (type.name().equals(text.trim().toUpperCase())) {
                return type;
            }
        }
        throw new IllegalArgumentException("invalid attraction type '" + text + "': use GENERAL, INDIVIDUAL, GROUP or"
                + " SPECIAL");
    }

    private static int parseCapacity(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid capacity '" + text + "': it must be a positive integer");
        }
    }

    private static BigDecimal parsePrice(String text) {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid price '" + text + "'");
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date '" + text + "': expected a real date as yyyy-MM-dd");
        }
    }

    private static UserField parseField(String name) {
        for (UserField f : UserField.values()) {
            if (f.name().equals(name.trim().toUpperCase())) {
                return f;
            }
        }
        throw new IllegalArgumentException("field '" + name + "' cannot be updated; valid fields are NAME, SURNAME,"
                + " EMAIL, PASSWORD, PHONE, CARD");
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException(
                    "expected " + expected + " parameters but got " + (parts.length - 1) + ". Usage: " + usage);
        }
    }
}
