package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for individual tickets. Date checks always use the shared {@link SystemDate}.
 */
public class TicketManager {
    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private final TicketRepository repository;
    private final UserManager userManager;
    private final SystemDate systemDate;
    private final List<Ticket> tickets;
    private BigDecimal price = DEFAULT_PRICE;

    public TicketManager(TicketRepository repository, UserManager userManager, SystemDate systemDate) {
        this.repository = repository;
        this.userManager = userManager;
        this.systemDate = systemDate;
        this.tickets = new ArrayList<>(repository.load(userManager::findByDni));
        userManager.addDeletionCheck(
                user -> hasCurrentOrFutureTickets(user) ? "the user has current or future tickets" : null);
    }

    public Ticket create(String id, String email, LocalDate date) {
        if (!UserValidator.isNotBlank(id)) {
            throw new IllegalArgumentException("the ticket identifier cannot be empty");
        }
        if (findById(id) != null) {
            throw new IllegalArgumentException("ticket " + id + " already exists");
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        if (date.isBefore(systemDate.get())) {
            throw new IllegalArgumentException("the ticket date " + date + " is before the system date "
                    + systemDate.get());
        }
        Ticket ticket = new Ticket(id, date, user, price);
        tickets.add(ticket);
        repository.save(tickets);
        return ticket;
    }

    public Ticket delete(String id) {
        Ticket ticket = findById(id);
        if (ticket == null) {
            throw new IllegalArgumentException("ticket " + id + " does not exist");
        }
        if (!ticket.isFuture(systemDate.get())) {
            throw new IllegalArgumentException("ticket " + id + " for " + ticket.getDate()
                    + " can only be deleted before its date (system date " + systemDate.get() + ")");
        }
        tickets.remove(ticket);
        repository.save(tickets);
        return ticket;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal newPrice) {
        if (newPrice.signum() <= 0 || newPrice.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("the price must be a positive amount with at most two decimals");
        }
        price = newPrice.setScale(2, RoundingMode.UNNECESSARY);
    }

    public boolean hasCurrentOrFutureTickets(User user) {
        for (Ticket t : tickets) {
            if (t.getUser() == user && t.isCurrentOrFuture(systemDate.get())) {
                return true;
            }
        }
        return false;
    }

    public Ticket findById(String id) {
        for (Ticket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }
}
