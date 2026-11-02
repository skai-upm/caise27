package es.upm.etsisi.amusementpark.park;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monetary amount parsing and formatting, without regular expressions.
 */
public final class Prices {

    private static final int MAX_DECIMALS = 2;

    private Prices() {
    }

    /**
     * Parses a positive amount written as digits with an optional {@code .} and at most two decimals.
     *
     * @return the amount, or {@code null} when the text is not a valid positive amount
     */
    public static BigDecimal parsePositive(String text) {
        if (text == null || text.isEmpty() || !hasAmountShape(text)) {
            return null;
        }
        BigDecimal amount = new BigDecimal(text);
        return amount.signum() > 0 ? amount.setScale(MAX_DECIMALS, RoundingMode.UNNECESSARY) : null;
    }

    public static String format(BigDecimal amount) {
        return amount.setScale(MAX_DECIMALS, RoundingMode.HALF_UP).toPlainString() + " EUR";
    }

    private static boolean hasAmountShape(String text) {
        int dot = text.indexOf('.');
        if (dot != text.lastIndexOf('.')) {
            return false;
        }
        String integer = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integer.isEmpty() || (dot >= 0 && decimals.isEmpty()) || decimals.length() > MAX_DECIMALS) {
            return false;
        }
        return isDigits(integer) && isDigits(decimals);
    }

    private static boolean isDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
