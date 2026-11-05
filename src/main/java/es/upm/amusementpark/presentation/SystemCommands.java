package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.ParkController;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * {@code SYSTEM DATE} and {@code SYSTEM PRICE} commands.
 */
public final class SystemCommands {

    private static final String DATE_ERROR = "Invalid date. Usage: SYSTEM DATE;<yyyy-MM-dd>";
    private static final String PRICE_ERROR =
            "Invalid price. Usage: SYSTEM PRICE;<Price> (positive amount, max. 2 decimals)";

    private final ParkController park;
    private final PrintStream out;

    private SystemCommands(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        SystemCommands commands = new SystemCommands(park, out);
        processor.register("SYSTEM DATE", commands::systemDate);
        processor.register("SYSTEM PRICE", commands::systemPrice);
    }

    private boolean systemDate(String line) {
        String[] parts = line.split(";", -1);
        LocalDate date = parts.length == 2 ? parseDate(parts[1].trim()) : null;
        if (date == null) {
            throw new InvalidCommandException(DATE_ERROR);
        }
        park.setCurrentDate(date);
        out.println("SYSTEM DATE UPDATED: " + date);
        return true;
    }

    private boolean systemPrice(String line) {
        String[] parts = line.split(";", -1);
        BigDecimal price = parts.length == 2 ? parsePrice(parts[1].trim()) : null;
        if (price == null) {
            throw new InvalidCommandException(PRICE_ERROR);
        }
        park.setCurrentPrice(price);
        out.println("SYSTEM PRICE UPDATED: " + park.getCurrentPrice().toPlainString());
        return true;
    }

    /**
     * Parses a strict {@code yyyy-MM-dd} date.
     *
     * @return the date, or {@code null} if invalid
     */
    public static LocalDate parseDate(String text) {
        return CommandSupport.parseDate(text);
    }

    /**
     * Parses a positive monetary amount with at most two decimals, using basic String operations only.
     *
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
