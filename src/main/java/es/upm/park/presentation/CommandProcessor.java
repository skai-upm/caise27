package es.upm.park.presentation;

import es.upm.park.business.ParkController;
import es.upm.park.business.model.Result;
import es.upm.park.business.model.UserField;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Presentation of the API-like text commands: parses each command and delegates the operation
 * to the {@link ParkController}. A command is split on {@code ;} into the operation and
 * its parameters; the operation is split on spaces into its words. Invalid input always
 * produces an error result and never throws.
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final String WORD_SEPARATOR = " ";

    private static final int DATE_LENGTH = "yyyy-MM-dd".length();

    private static final int PRICE_DECIMALS = 2;

    private final ParkController controller;
    private final HelpGenerator basicHelp;
    private final HelpGenerator extendedHelp;

    /** Receives the controller and the help generators configured at startup. */
    public CommandProcessor(ParkController controller, HelpGenerator basicHelp, HelpGenerator extendedHelp) {
        this.controller = controller;
        this.basicHelp = basicHelp;
        this.extendedHelp = extendedHelp;
    }

    public ParkController getController() {
        return controller;
    }

    public Result process(String line) {
        if (line == null || line.isBlank()) {
            return Result.error("Empty command.");
        }
        String[] parts = line.split(PARAM_SEPARATOR, -1);
        String[] params = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            params[i - 1] = parts[i].trim();
        }
        List<String> words = new ArrayList<>();
        for (String w : parts[0].trim().split(WORD_SEPARATOR)) {
            if (!w.isEmpty()) {
                words.add(w);
            }
        }
        if (words.size() == 1 && words.get(0).equals("HELP")) {
            return params.length == 0 ? Result.ok(basicHelp.generate()) : Result.error("Usage: HELP");
        }
        if (words.size() == 2 && words.get(0).equals("HELP") && words.get(1).equals("EXTENDED")) {
            return params.length == 0 ? Result.ok(extendedHelp.generate()) : Result.error("Usage: HELP EXTENDED");
        }
        if (words.size() == 1 && words.get(0).equals("EXIT")) {
            return params.length == 0 ? Result.exit("Bye.") : Result.error("Usage: EXIT");
        }
        if (words.size() == 2 && words.get(0).equals("SYSTEM") && words.get(1).equals("DATE")) {
            return systemDate(params);
        }
        if (words.size() == 2 && words.get(0).equals("SYSTEM") && words.get(1).equals("PRICE")) {
            return systemPrice(params);
        }
        if (words.size() == 2 && words.get(0).equals("TICKET")) {
            return ticket(words.get(1), params, parts[0].trim());
        }
        if (words.size() == 2 && words.get(0).equals("GROUPTICKET")) {
            return groupTicket(words.get(1), params, parts[0].trim());
        }
        if (words.size() == 2 && (words.get(0).equals("SPEEDPASS") || words.get(0).equals("SPECIALPASS"))) {
            return pass(words.get(0), words.get(1), params, parts[0].trim());
        }
        if (words.size() == 2 && words.get(0).equals("ATTRACTION")) {
            return attraction(words.get(1), params, parts[0].trim());
        }
        if (words.size() == 2 && words.get(0).equals("INVOICE") && words.get(1).equals("CREATE")) {
            return invoice(params);
        }
        if (words.size() == 2 && words.get(0).equals("GROUP")) {
            return group(words.get(1), params, parts[0].trim());
        }
        if (words.size() != 2 || !words.get(0).equals("USER")) {
            return Result.error("Unknown command: " + parts[0].trim());
        }
        return switch (words.get(1)) {
            case "CREATE" -> userCreate(params);
            case "DELETE" -> userDelete(params);
            case "UPDATE" -> userUpdate(params);
            default -> Result.error("Unknown command: " + parts[0].trim());
        };
    }

    private Result systemDate(String[] p) {
        if (!hasParams(p, 1)) {
            return Result.error("Usage: SYSTEM DATE;yyyy-MM-dd");
        }
        Optional<LocalDate> date = parseDate(p[0]);
        if (date.isEmpty()) {
            return Result.error("Invalid date '" + p[0] + "': expected format yyyy-MM-dd.");
        }
        return controller.setDate(date.get());
    }

    private Result systemPrice(String[] p) {
        if (!hasParams(p, 1)) {
            return Result.error("Usage: SYSTEM PRICE;Precio");
        }
        Optional<BigDecimal> price = parsePrice(p[0]);
        if (price.isEmpty()) {
            return Result.error("Invalid price '" + p[0] + "': expected a non-negative amount such as 60.00.");
        }
        return controller.setPrice(price.get());
    }

    private Result ticket(String operation, String[] p, String command) {
        switch (operation) {
            case "CREATE" -> {
                if (!hasParams(p, 3)) {
                    return Result.error("Usage: TICKET CREATE;Id;Correo;yyyy-MM-dd");
                }
                Optional<LocalDate> date = parseDate(p[2]);
                if (date.isEmpty()) {
                    return Result.error("Invalid date '" + p[2] + "': expected format yyyy-MM-dd.");
                }
                return controller.createTicket(p[0], p[1], date.get());
            }
            case "DELETE" -> {
                return hasParams(p, 1) ? controller.deleteTicket(p[0]) : Result.error("Usage: TICKET DELETE;Id");
            }
            default -> {
                return Result.error("Unknown command: " + command);
            }
        }
    }

    private Result groupTicket(String operation, String[] p, String command) {
        switch (operation) {
            case "CREATE" -> {
                if (!hasParams(p, 3)) {
                    return Result.error("Usage: GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
                }
                Optional<LocalDate> date = parseDate(p[2]);
                if (date.isEmpty()) {
                    return Result.error("Invalid date '" + p[2] + "': expected format yyyy-MM-dd.");
                }
                return controller.createGroupTicket(p[0], p[1], date.get());
            }
            case "DELETE" -> {
                return hasParams(p, 1) ? controller.deleteGroupTicket(p[0])
                        : Result.error("Usage: GROUPTICKET DELETE;Id");
            }
            default -> {
                return Result.error("Unknown command: " + command);
            }
        }
    }

    private Result attraction(String operation, String[] p, String command) {
        return switch (operation) {
            case "CREATE" -> hasParams(p, 3) ? controller.createAttraction(p[0], p[1], p[2])
                    : Result.error("Usage: ATTRACTION CREATE;Id;Nombre;Capacidad");
            case "ENTER" -> hasParams(p, 2)
                    ? controller.enterAttraction(p[0], p[1])
                    : Result.error("Usage: ATTRACTION ENTER;IdAtraccion;IdTicket");
            case "EXIT" -> hasParams(p, 2)
                    ? controller.exitAttraction(p[0], p[1])
                    : Result.error("Usage: ATTRACTION EXIT;IdAtraccion;IdTicket");
            case "STATUS" -> hasParams(p, 1) ? controller.attractionStatus(p[0])
                    : Result.error("Usage: ATTRACTION STATUS;IdAtraccion");
            default -> Result.error("Unknown command: " + command);
        };
    }

    private Result pass(String kind, String operation, String[] p, String command) {
        boolean speed = kind.equals("SPEEDPASS");
        if (operation.equals("CREATE")) {
            if (speed) {
                return hasParams(p, 3) ? controller.buySpeedPasses(p[0], p[1], p[2])
                        : Result.error("Usage: SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad");
            }
            return hasParams(p, 2) ? controller.buySpecialPass(p[0], p[1])
                    : Result.error("Usage: SPECIALPASS CREATE;Id;IdTicket");
        }
        if (operation.equals("DELETE")) {
            if (!hasParams(p, 1)) {
                return Result.error("Usage: " + kind + " DELETE;IdPase");
            }
            return speed ? controller.refundSpeedPass(p[0]) : controller.refundSpecialPass(p[0]);
        }
        return Result.error("Unknown command: " + command);
    }

    private Result invoice(String[] p) {
        if (!hasParams(p, 2)) {
            return Result.error("Usage: INVOICE CREATE;USER;Correo or INVOICE CREATE;GROUP;IdGrupo");
        }
        return switch (p[0]) {
            case "USER" -> controller.createUserInvoice(p[1]);
            case "GROUP" -> controller.createGroupInvoice(p[1]);
            default -> Result.error("Usage: INVOICE CREATE;USER;Correo or INVOICE CREATE;GROUP;IdGrupo");
        };
    }

    /** Strict {@code yyyy-MM-dd} calendar date. */
    private static Optional<LocalDate> parseDate(String text) {
        if (text.length() != DATE_LENGTH) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** Non-negative amount: digits, optionally a point and at most two decimals. */
    private static Optional<BigDecimal> parsePrice(String text) {
        int point = text.indexOf('.');
        String whole = point < 0 ? text : text.substring(0, point);
        String decimals = point < 0 ? "" : text.substring(point + 1);
        if (whole.isEmpty() || !isAsciiDigits(whole) || !isAsciiDigits(decimals)
                || decimals.length() > PRICE_DECIMALS || (point >= 0 && decimals.isEmpty())) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(text).setScale(PRICE_DECIMALS, RoundingMode.UNNECESSARY));
    }

    private static boolean isAsciiDigits(String s) {
        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private Result userCreate(String[] p) {
        if (!hasParams(p, 7)) {
            return Result.error("Usage: USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        return controller.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
    }

    private Result userDelete(String[] p) {
        if (!hasParams(p, 2)) {
            return Result.error("Usage: USER DELETE;Correo;Contraseña");
        }
        return controller.deleteUser(p[0], p[1]);
    }

    private Result group(String operation, String[] p, String command) {
        return switch (operation) {
            case "CREATE" -> hasParams(p, 2) ? controller.createGroup(p[0], p[1]) : Result.error("Usage: GROUP CREATE;Id;Nombre");
            case "ADD" -> hasParams(p, 2) ? controller.addToGroup(p[0], p[1]) : Result.error("Usage: GROUP ADD;Id;Correo");
            case "REMOVE" -> hasParams(p, 2) ? controller.removeFromGroup(p[0], p[1]) : Result.error("Usage: GROUP REMOVE;Id;Correo");
            case "DELETE" -> hasParams(p, 1) ? controller.deleteGroup(p[0]) : Result.error("Usage: GROUP DELETE;Id");
            default -> Result.error("Unknown command: " + command);
        };
    }

    private Result userUpdate(String[] p) {
        if (!hasParams(p, 4)) {
            return Result.error("Usage: USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field;
        try {
            field = UserField.valueOf(p[2]);
        } catch (IllegalArgumentException e) {
            return Result.error("Field " + p[2] + " cannot be updated. Editable fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD.");
        }
        return controller.updateUser(p[0], p[1], field, p[3]);
    }

    /** Exactly {@code count} parameters, none of them empty. */
    private static boolean hasParams(String[] p, int count) {
        if (p.length != count) {
            return false;
        }
        for (String s : p) {
            if (s.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
