package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SYSTEM DATE and SYSTEM PRICE. Single instance per execution.
 */
public final class SystemController {

    private static final SystemController INSTANCE = new SystemController();

    private SystemState state;

    private SystemController() {
    }

    public static SystemController getInstance() {
        return INSTANCE;
    }

    public void configure(SystemState state) {
        this.state = state;
    }

    public LocalDate setDate(String text) {
        LocalDate date = Checks.parseDate(text);
        state().setCurrentDate(date);
        return date;
    }

    public BigDecimal setPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid price: " + text);
        }
        state().setBasePrice(price);
        return state().getBasePrice();
    }

    private SystemState state() {
        return Checks.configured(state, "SystemController");
    }
}
