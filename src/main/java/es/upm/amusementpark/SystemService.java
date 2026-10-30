package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SYSTEM DATE and SYSTEM PRICE operations.
 */
public class SystemService {

    private final Park park = Park.getInstance();

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
