package es.upm.etsisi.park.model.ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Euro amounts with two decimals. */
public final class Money {

    private static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal of(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal amount) {
        return of(amount).toPlainString() + " EUR";
    }

    /**
     * Parses a positive amount written as digits with an optional '.' and at
     * most two decimals, or returns null if the text is not such an amount.
     */
    public static BigDecimal parsePositive(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        int dot = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.') {
                if (dot >= 0) {
                    return null;
                }
                dot = i;
            } else if (c < '0' || c > '9') {
                return null;
            }
        }
        if (dot == 0 || dot == text.length() - 1 || (dot > 0 && text.length() - dot - 1 > SCALE)) {
            return null;
        }
        BigDecimal amount = new BigDecimal(text);
        return amount.signum() > 0 ? of(amount) : null;
    }
}
