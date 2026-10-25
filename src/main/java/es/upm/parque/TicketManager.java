package es.upm.parque;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business rules for individual tickets. All date checks use the shared system date.
 */
public class TicketManager {

    static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");
    static final String TICKET_CREATED = "OK TICKET CREATED -> ";
    static final String TICKET_DELETED = "OK TICKET DELETED";
    static final String PRICE_SET = "OK SYSTEM PRICE -> ";
    static final String DUPLICATE_TICKET = "ERROR DUPLICATE TICKET";
    static final String TICKET_NOT_FOUND = "ERROR TICKET NOT FOUND";
    static final String NOT_REFUNDABLE = "ERROR TICKET NOT REFUNDABLE";
    static final String PAST_DATE = "ERROR PAST DATE";
    static final String INVALID_DATE = "ERROR INVALID DATE";
    static final String INVALID_PRICE = "ERROR INVALID PRICE";

    private final TicketRepository repository;
    private final UserManager users;
    private final SystemDate systemDate;
    private final Map<String, Ticket> ticketsById = new LinkedHashMap<>();
    private BigDecimal price = INITIAL_PRICE;

    public TicketManager(TicketRepository repository, UserManager users, SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.users = users;
        this.systemDate = systemDate;
        for (Ticket ticket : repository.load(users::findByDni)) {
            ticketsById.put(ticket.getId(), ticket);
        }
    }

    /**
     * Parses a positive amount with two decimals, or returns null when the text is not valid.
     */
    static BigDecimal parsePrice(String text) {
        try {
            BigDecimal value = new BigDecimal(text);
            return value.signum() > 0 ? value.setScale(2, RoundingMode.HALF_UP) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String setPrice(String text) {
        BigDecimal value = parsePrice(text);
        if (value == null) {
            return INVALID_PRICE;
        }
        price = value;
        return PRICE_SET + price.toPlainString();
    }

    public String create(String id, String email, String dateText) {
        if (!UserValidator.isNotBlank(id)) {
            return UserManager.EMPTY_FIELD;
        }
        if (ticketsById.containsKey(id)) {
            return DUPLICATE_TICKET;
        }
        User holder = users.findByEmail(email);
        if (holder == null) {
            return GroupManager.USER_NOT_FOUND;
        }
        LocalDate date = SystemDate.parse(dateText);
        if (date == null) {
            return INVALID_DATE;
        }
        if (date.isBefore(systemDate.get())) {
            return PAST_DATE;
        }
        ticketsById.put(id, new Ticket(id, date, holder, price));
        return persist(TICKET_CREATED + price.toPlainString());
    }

    public String delete(String id) {
        Ticket ticket = ticketsById.get(id);
        if (ticket == null) {
            return TICKET_NOT_FOUND;
        }
        if (!ticket.isRefundable(systemDate.get())) {
            return NOT_REFUNDABLE;
        }
        ticketsById.remove(id);
        return persist(TICKET_DELETED);
    }

    /**
     * True when the user holds any ticket dated on or after the system date.
     */
    public boolean hasActiveTickets(User user) {
        LocalDate today = systemDate.get();
        return ticketsById.values().stream()
                .anyMatch(t -> t.getHolder() == user && t.isCurrentOrFuture(today));
    }

    public Ticket find(String id) {
        return ticketsById.get(id);
    }

    private String persist(String success) {
        try {
            repository.save(ticketsById.values());
            return success;
        } catch (IOException e) {
            return UserManager.STORAGE_FAILURE;
        }
    }
}
