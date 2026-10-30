package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.ParkFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SYSTEM DATE and SYSTEM PRICE (Singleton).
 */
public final class SystemController {

    private static SystemController instance;

    private Park park;

    private SystemController() {
    }

    public static synchronized SystemController getInstance() {
        if (instance == null) {
            instance = new SystemController();
        }
        return instance;
    }

    public void configure(Park park) {
        this.park = park;
    }

    public LocalDate setDate(String text) {
        LocalDate date = ParkFormat.parseDate(text);
        park.setDate(date);
        return date;
    }

    public BigDecimal setPrice(String text) {
        BigDecimal price = ParkFormat.parsePrice(text);
        park.setPrice(price);
        return price;
    }
}
