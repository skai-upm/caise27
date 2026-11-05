package es.upm.parque.business;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Business operations on the park-wide system values: current date and base ticket price.
 */
public class SystemManager {

    static final String DATE_EXPECTED = "SYSTEM DATE expects a valid date: SYSTEM DATE;<yyyy-MM-dd>";
    static final String PRICE_EXPECTED =
            "SYSTEM PRICE expects a positive amount with at most 2 decimals: SYSTEM PRICE;<price>";

    private ParkController park;

    private SystemManager() {
    }

    private static final class Holder {
        private static final SystemManager INSTANCE = new SystemManager();
    }

    /** Returns the only instance of this controller. */
    public static SystemManager getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management configured during application initialization. */
    public void inject(ParkController park) {
        this.park = park;
    }

    private ParkController park() {
        return ParkConfiguration.requireInjected(park);
    }

    /** Sets the current system date from its {@code yyyy-MM-dd} text. */
    public LocalDate setDate(String text) throws ParkException {
        LocalDate date = SystemValues.parseDate(text);
        if (date == null) {
            throw new InvalidDataException(DATE_EXPECTED);
        }
        park().setCurrentDate(date);
        return date;
    }

    /** Sets the base ticket price from a positive amount with at most two decimals. */
    public BigDecimal setPrice(String text) throws ParkException {
        BigDecimal price = SystemValues.parsePrice(text);
        if (price == null) {
            throw new InvalidDataException(PRICE_EXPECTED);
        }
        park().setBasePrice(price);
        return price;
    }
}
