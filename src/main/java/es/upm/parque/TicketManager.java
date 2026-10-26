package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for individual tickets. All date checks use the shared system date.
 */
public class TicketManager {

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private final TicketRepository repository;
    private final UserManager userManager;
    private final SystemDate systemDate;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private BigDecimal currentPrice = DEFAULT_PRICE;

    public TicketManager(TicketRepository repository, UserManager userManager, SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.userManager = userManager;
        this.systemDate = systemDate;
        for (Ticket ticket : repository.loadAll(userManager::findByDni)) {
            tickets.put(ticket.getId(), ticket);
        }
    }

    public Ticket find(String id) {
        return tickets.get(id);
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the price of tickets sold from now on. It must be positive with at most two decimals.
     */
    public void setCurrentPrice(String amount) {
        BigDecimal price;
        try {
            price = new BigDecimal(amount.trim());
        } catch (NumberFormatException e) {
            throw new UserException("Invalid price: " + amount);
        }
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new UserException("Invalid price: " + amount + " (must be positive with at most two decimals)");
        }
        currentPrice = price.setScale(2);
    }

    public Ticket create(String id, String email, String dateText) {
        if (id == null || id.isBlank()) {
            throw new UserException("The ticket identifier cannot be empty");
        }
        if (tickets.containsKey(id)) {
            throw new UserException("A ticket with identifier " + id + " already exists");
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new UserException("No user with email " + email);
        }
        LocalDate date = SystemDate.parse(dateText.trim());
        if (date.isBefore(systemDate.get())) {
            throw new UserException("Cannot buy a ticket for a past date: " + dateText);
        }
        Ticket ticket = new Ticket(id, date, user, currentPrice);
        tickets.put(id, ticket);
        save();
        return ticket;
    }

    public void delete(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new UserException("No ticket with identifier " + id);
        }
        if (!ticket.isFuture(systemDate.get())) {
            throw new UserException("Ticket " + id + " can only be deleted before its date");
        }
        tickets.remove(id);
        save();
    }

    /**
     * True if the user holds a ticket dated on or after the system date.
     */
    public boolean hasCurrentOrFutureTicket(User user) {
        LocalDate today = systemDate.get();
        return tickets.values().stream()
                .anyMatch(ticket -> ticket.getUser() == user && ticket.isCurrentOrFuture(today));
    }

    private void save() {
        try {
            repository.saveAll(tickets.values());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save tickets", e);
        }
    }
}
