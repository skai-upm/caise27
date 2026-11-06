package es.upm.park.command;

import es.upm.park.controller.ParkController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Interprets one command line, independently of where the line comes from.
 * A line is split on ';': the first segment is the command keyword and the
 * remaining segments are its arguments.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "HELP",
            "EXIT");

    static final String EXIT_TEXT = "Exiting the application.";

    private final Map<String, Function<String[], CommandResult>> handlers = new LinkedHashMap<>();

    public CommandProcessor() {
        handlers.put("HELP", args -> CommandResult.of(HELP_TEXT));
        handlers.put("EXIT", args -> CommandResult.exit(EXIT_TEXT));
        handlers.put("SYSTEM DATE", this::systemDate);
        handlers.put("SYSTEM PRICE", this::systemPrice);
    }

    public CommandResult process(String line) {
        String[] parts = (line == null ? "" : line).split(";", -1);
        String keyword = parts[0].trim();
        Function<String[], CommandResult> handler = handlers.get(keyword);
        if (handler == null) {
            return error("Unknown command: " + (line == null ? "" : line.trim()));
        }
        return handler.apply(Arrays.copyOfRange(parts, 1, parts.length));
    }

    private CommandResult systemDate(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            return error("Usage: SYSTEM DATE;<yyyy-MM-dd>");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(args[0].trim());
        } catch (DateTimeParseException e) {
            return error("Invalid date, expected yyyy-MM-dd: " + args[0].trim());
        }
        ParkController.getInstance().setCurrentDate(date);
        return CommandResult.of("SYSTEM DATE UPDATED: " + date);
    }

    private CommandResult systemPrice(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            return error("Usage: SYSTEM PRICE;<Price>");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(args[0].trim());
        } catch (NumberFormatException e) {
            return error("Invalid price: " + args[0].trim());
        }
        try {
            ParkController.getInstance().setBasePrice(price);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
        return CommandResult.of("SYSTEM PRICE UPDATED: " + ParkController.getInstance().getBasePrice());
    }

    private static CommandResult error(String message) {
        return CommandResult.of("ERROR: " + message);
    }
}
