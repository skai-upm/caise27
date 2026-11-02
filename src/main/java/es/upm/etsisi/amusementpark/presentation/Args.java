package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.park.Prices;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Conversion of textual command arguments.
 */
final class Args {

    private Args() {
    }

    static LocalDate date(String text) throws CommandSyntaxException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new CommandSyntaxException("invalid date '" + text + "', expected yyyy-MM-dd");
        }
    }

    static int integer(String text, String what) throws CommandSyntaxException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new CommandSyntaxException(what + " must be an integer");
        }
    }

    static BigDecimal price(String text) throws CommandSyntaxException {
        BigDecimal price = Prices.parsePositive(text);
        if (price == null) {
            throw new CommandSyntaxException("invalid price '" + text + "', expected a positive amount such as 45.50");
        }
        return price;
    }
}
