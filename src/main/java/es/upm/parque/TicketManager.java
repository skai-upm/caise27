package es.upm.parque;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for individual tickets. Dates are always compared with the system date held by
 * the {@link MainController}. Every successful change is persisted.
 */
public class TicketManager {

    private final TicketRepository repository;
    private final UserManager userManager;
    private final MainController mainController;
    private List<Ticket> tickets;

    public TicketManager(TicketRepository repository, UserManager userManager, MainController mainController) {
        this.repository = repository;
        this.userManager = userManager;
        this.mainController = mainController;
        this.tickets = load();
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    public Ticket findById(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Buys a ticket for the user with the given email at the current price and returns it.
     */
    public Ticket create(String id, String email, String dateText) {
        if (!UserValidator.isNotBlank(id)) {
            throw new ParkException("The ticket identifier is required");
        }
        if (findById(id) != null) {
            throw new ParkException("A ticket with identifier " + id + " already exists");
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new ParkException("No user with email " + email);
        }
        LocalDate date = MainController.parseDate(dateText);
        if (date.isBefore(mainController.getCurrentDate())) {
            throw new ParkException("Cannot buy a ticket for a past date: " + dateText);
        }
        Ticket ticket = new Ticket(id, date, user, mainController.getTicketPrice());
        tickets.add(ticket);
        persist();
        return ticket;
    }

    /**
     * Deletes a ticket whose date is strictly later than the system date.
     */
    public void delete(String id) {
        Ticket ticket = findById(id);
        if (ticket == null) {
            throw new ParkException("No ticket with identifier " + id);
        }
        if (!ticket.getDate().isAfter(mainController.getCurrentDate())) {
            throw new ParkException("Only tickets dated after the system date can be deleted");
        }
        tickets.remove(ticket);
        persist();
    }

    /**
     * True if the user has a ticket dated on or after the system date.
     */
    public boolean hasCurrentOrFutureTicket(User user) {
        LocalDate today = mainController.getCurrentDate();
        return tickets.stream().anyMatch(t -> t.getUser() == user && !t.getDate().isBefore(today));
    }

    /**
     * Removes every ticket of a user that is being deleted.
     */
    public void removeTicketsOf(User user) {
        if (tickets.removeIf(t -> t.getUser() == user)) {
            persist();
        }
    }

    private List<Ticket> load() {
        Map<String, User> usersByDni = new HashMap<>();
        for (User u : userManager.getUsers()) {
            usersByDni.put(u.getDni(), u);
        }
        return new ArrayList<>(repository.loadAll(usersByDni));
    }

    private void persist() {
        try {
            repository.saveAll(tickets);
        } catch (UncheckedIOException e) {
            tickets = load();
            throw new ParkException("Could not save tickets: " + e.getMessage());
        }
    }
}
