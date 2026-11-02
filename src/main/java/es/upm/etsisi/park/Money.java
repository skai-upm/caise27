package es.upm.etsisi.park;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monetary helpers: amounts are euros with two decimals, rounded half-up.
 */
public final class Money {

    private static final int SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Money() {
    }

    public static BigDecimal of(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal discounted(BigDecimal base, int percent) {
        return of(base.multiply(HUNDRED.subtract(BigDecimal.valueOf(percent))).divide(HUNDRED, SCALE, RoundingMode.HALF_UP));
    }

    public static String format(BigDecimal amount) {
        return of(amount).toPlainString() + " EUR";
    }
}
