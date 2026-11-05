package es.upm.amusementpark.park;

import es.upm.amusementpark.cli.CommandProcessor;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * {@code SYSTEM DATE} and {@code SYSTEM PRICE} commands.
 */
public final class SystemCommands {

    private final ParkController park;
    private final PrintStream out;

    private SystemCommands(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /**
     * Registers the SYSTEM commands in the processor.
     *
     * @param processor shared command processor
     * @param park central park management
     * @param out output stream
     */
    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        SystemCommands commands = new SystemCommands(park, out);
        processor.register("SYSTEM DATE", commands::systemDate);
        processor.register("SYSTEM PRICE", commands::systemPrice);
    }

    private boolean systemDate(String line) {
        String[] parts = line.split(";", -1);
        LocalDate date = parts.length == 2 ? parseDate(parts[1].trim()) : null;
        if (date == null) {
            out.println("ERROR: Invalid date. Usage: SYSTEM DATE;<yyyy-MM-dd>");
            return true;
        }
        park.setCurrentDate(date);
        out.println("SYSTEM DATE UPDATED: " + date);
        return true;
    }

    private boolean systemPrice(String line) {
        String[] parts = line.split(";", -1);
        BigDecimal price = parts.length == 2 ? parsePrice(parts[1].trim()) : null;
        if (price == null) {
            out.println("ERROR: Invalid price. Usage: SYSTEM PRICE;<Price> (positive amount, max. 2 decimals)");
            return true;
        }
        park.setCurrentPrice(price);
        out.println("SYSTEM PRICE UPDATED: " + park.getCurrentPrice().toPlainString());
        return true;
    }

    /**
     * Parses a strict {@code yyyy-MM-dd} date.
     *
     * @param text date text
     * @return the date, or {@code null} if invalid
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != 10) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a positive monetary amount with at most two decimals, using basic String operations only.
     *
     * @param text amount text
     * @return the amount, or {@code null} if invalid
     */
    public static BigDecimal parsePrice(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        int dot = text.indexOf('.');
        if (dot != text.lastIndexOf('.') || dot == 0 || dot == text.length() - 1
                || (dot > 0 && text.length() - dot - 1 > 2)) {
            return null;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i != dot && (c < '0' || c > '9')) {
                return null;
            }
        }
        BigDecimal price = new BigDecimal(text);
        return price.signum() > 0 ? price : null;
    }
}
