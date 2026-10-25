package com.amusementpark.business.controller;

import com.amusementpark.business.validation.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Main controller holding the park-wide settings: the single simulated system date
 * and the current individual ticket price. Neither is persisted.
 */
public final class ParkController {
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");
    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Starts the settings for a new run.
     */
    public void configure(LocalDate startDate) {
        this.currentDate = startDate;
        this.ticketPrice = DEFAULT_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public String setDate(String text) {
        LocalDate date = Validator.parseDate(text);
        if (date == null) {
            return "ERROR: invalid date, expected yyyy-MM-dd";
        }
        currentDate = date;
        return "OK: system date set to " + date;
    }

    public String setPrice(String text) {
        BigDecimal price = Validator.parsePrice(text);
        if (price == null) {
            return "ERROR: invalid price, expected a positive amount with up to two decimals";
        }
        ticketPrice = price.setScale(2, RoundingMode.HALF_UP);
        return "OK: ticket price set to " + format(ticketPrice);
    }

    public static String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
