package es.upm.park.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import es.upm.park.model.Group;
import es.upm.park.model.User;
import es.upm.park.repository.GroupRepository;
import es.upm.park.repository.TicketRepository;
import es.upm.park.repository.UserRepository;
import es.upm.park.service.GroupException;
import es.upm.park.service.GroupService;
import es.upm.park.service.ParkException;
import es.upm.park.service.TicketException;
import es.upm.park.service.TicketService;
import es.upm.park.service.UserService;

/**
 * Central management of the park. Exactly one instance exists; it keeps the
 * overall park state (elements, current date and base ticket price) and gives
 * access to the services operating on it.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");
    private static final int PRICE_DECIMALS = 2;

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal basePrice = INITIAL_PRICE;
    private UserService userService;
    private GroupService groupService;
    private TicketService ticketService;

    private ParkController() {
        start(new UserRepository(), LocalDate.now());
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
        this.groupService = new GroupService(new GroupRepository(), users, group -> false);
        this.ticketService = new TicketService(new TicketRepository(), users, this::getCurrentDate, this::getBasePrice);
        this.currentDate = today;
        this.basePrice = INITIAL_PRICE;
    }

    public UserService getUserService() {
        return userService;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public TicketService getTicketService() {
        return ticketService;
    }

    /**
     * Deletes a user after checking credentials, active tickets and locked
     * groups; the user is also removed from every group it belongs to.
     *
     * @throws ParkException when the deletion is not allowed
     */
    public User deleteUser(String email, String password) {
        User user = userService.authenticate(email, password);
        if (ticketService.hasActiveTicket(user)) {
            throw new TicketException("user " + email + " has an active ticket");
        }
        for (Group group : groupService.groupsOf(user)) {
            if (groupService.isLocked(group)) {
                throw new GroupException("user " + email + " belongs to group " + group.getId()
                        + " which has an active group ticket");
            }
        }
        userService.remove(user);
        groupService.groupsOf(user).forEach(group -> group.removeMember(user.getDni()));
        return user;
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
     * @throws ParkException when the text is not a valid date
     */
    public LocalDate setCurrentDate(String text) {
        currentDate = Dates.parse(text);
        return currentDate;
    }

    /**
     * Replaces the base ticket price with a positive amount of at most two
     * decimals.
     *
     * @throws ParkException when the text is not a valid positive amount
     */
    public BigDecimal setBasePrice(String text) {
        if (!isMoney(text)) {
            throw new ParkException("invalid price " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(PRICE_DECIMALS, RoundingMode.UNNECESSARY);
        if (price.signum() <= 0) {
            throw new ParkException("invalid price " + text);
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
