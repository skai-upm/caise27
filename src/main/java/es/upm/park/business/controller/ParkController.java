package es.upm.park.business.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.repository.ParkRepositories;
import es.upm.park.persistence.repository.UserRepository;
import es.upm.park.business.service.AttractionService;
import es.upm.park.business.service.GroupException;
import es.upm.park.business.invoice.TextInvoiceBuilder;
import es.upm.park.business.service.GroupService;
import es.upm.park.business.service.InvoiceService;
import es.upm.park.business.service.ParkException;
import es.upm.park.business.service.TicketException;
import es.upm.park.business.service.TicketService;
import es.upm.park.business.service.UserService;

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
    private AttractionService attractionService;
    private InvoiceService invoiceService;

    private ParkController() {
        start(ParkRepositories.inMemory(), LocalDate.now());
    }

    /** The single central management instance. */
    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Starts a new execution with in-memory repositories around the given
     * users (convenience for embedding and tests).
     */
    public void start(UserRepository users, LocalDate today) {
        start(ParkRepositories.inMemory(users), today);
    }

    /**
     * Starts a new execution: injects the repositories configured during
     * application initialization into the singleton controllers, wires their
     * collaborations, resets the date to {@code today} and the price to 40.00.
     */
    public void start(ParkRepositories repositories, LocalDate today) {
        this.userService = UserService.getInstance().configure(repositories.users());
        this.ticketService = TicketService.getInstance().configure(repositories.tickets(), repositories.users(),
                this::getCurrentDate, this::getBasePrice);
        TicketService tickets = this.ticketService;
        this.groupService = GroupService.getInstance().configure(repositories.groups(), repositories.users(),
                tickets::hasActiveGroupTicket);
        this.attractionService = AttractionService.getInstance().configure(repositories.attractions());
        this.invoiceService = InvoiceService.getInstance().configure(repositories.users(), repositories.groups(),
                repositories.tickets(), new TextInvoiceBuilder());
        this.currentDate = today;
        this.basePrice = INITIAL_PRICE;
    }

    public UserService getUserService() {
        return userService;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public AttractionService getAttractionService() {
        return attractionService;
    }

    public InvoiceService getInvoiceService() {
        return invoiceService;
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
        groupService.removeFromAllGroups(user);
        return user;
    }

    /**
     * Deletes a group without active group tickets; its historical group
     * tickets are kept with their group reference cleared.
     */
    public void deleteGroup(String id) {
        Group group = groupService.find(id);
        groupService.delete(id);
        ticketService.detachGroup(group);
    }

    /** Buys a group ticket for the current members of the group. */
    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        return ticketService.createGroupTicket(id, groupService.find(groupId), date);
    }

    /**
     * Uses a ticket to let its users into an attraction on the current date.
     *
     * @return the users who entered
     */
    public List<User> enterAttraction(String attractionId, String ticketId) {
        attractionService.find(attractionId);
        return attractionService.enter(attractionId, ticketService.findForAccess(ticketId), currentDate);
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
