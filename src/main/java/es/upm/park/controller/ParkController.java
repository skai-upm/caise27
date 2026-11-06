package es.upm.park.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import es.upm.park.repository.UserRepository;
import es.upm.park.service.UserException;
import es.upm.park.service.UserService;

/**
 * Central management of the park. Exactly one instance exists; it keeps the
 * overall park state (elements, current date and base ticket price) and gives
 * access to the services operating on it.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int PRICE_DECIMALS = 2;

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal basePrice = INITIAL_PRICE;
    private UserService userService = new UserService(new UserRepository());

    private ParkController() {
    }

    /** The single central management instance. */
    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Starts a new execution: resets the date to {@code today}, the base price
     * to 40.00 and attaches the stored users.
     */
    public void start(UserRepository users, LocalDate today) {
        this.userService = new UserService(users);
        this.currentDate = today;
        this.basePrice = INITIAL_PRICE;
    }

    public UserService getUserService() {
        return userService;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Replaces the current date with a strict {@code yyyy-MM-dd} value.
     *
     * @throws UserException when the text is not a valid date
     */
    public LocalDate setCurrentDate(String text) {
        try {
            currentDate = LocalDate.parse(text, DATE_FORMAT);
            return currentDate;
        } catch (DateTimeParseException | NullPointerException e) {
            throw new UserException("invalid date " + text);
        }
    }

    /**
     * Replaces the base ticket price with a positive amount of at most two
     * decimals.
     *
     * @throws UserException when the text is not a valid positive amount
     */
    public BigDecimal setBasePrice(String text) {
        if (!isMoney(text)) {
            throw new UserException("invalid price " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(PRICE_DECIMALS, RoundingMode.UNNECESSARY);
        if (price.signum() <= 0) {
            throw new UserException("invalid price " + text);
        }
        basePrice = price;
        return basePrice;
    }

    private static boolean isMoney(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        boolean decimalsOk = dot < 0 || (!decimals.isEmpty() && decimals.length() <= PRICE_DECIMALS);
        return !whole.isEmpty() && decimalsOk && digitsOnly(whole) && digitsOnly(decimals);
    }

    private static boolean digitsOnly(String text) {
        return text.chars().allMatch(c -> c >= '0' && c <= '9');
    }
}
