package es.upm.etsisi.park.service;

import es.upm.etsisi.park.core.SystemState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * SYSTEM DATE and SYSTEM PRICE.
 */
public class SystemService {

    private final SystemState state;

    public SystemService(SystemState state) {
        this.state = state;
    }

    public LocalDate setDate(String text) {
        LocalDate date = Dates.parse(text);
        state.setCurrentDate(date);
        return date;
    }

    public BigDecimal setPrice(String text) {
        try {
            state.setBasePrice(new BigDecimal(text));
        } catch (NumberFormatException e) {
            throw new UserOperationException("Invalid price: " + text);
        } catch (IllegalArgumentException e) {
            throw new UserOperationException(e.getMessage());
        }
        return state.getBasePrice();
    }

    /** Date parsing shared by the services: strict ISO {@code yyyy-MM-dd}. */
    static final class Dates {

        private Dates() {
        }

        static LocalDate parse(String text) {
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                throw new UserOperationException("Invalid date (expected yyyy-MM-dd): " + text);
            }
        }
    }
}
