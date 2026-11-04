package es.upm.park;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Creates and deletes individual tickets. Date rules always use the date supplied by the
 * controller (the configured system date). Every successful change is saved before it
 * becomes visible in memory.
 */
public class TicketManager {

    private final TicketFileStore store;
    private final UserManager users;
    private final List<String> historicalLines;
    private List<Ticket> tickets;

    public TicketManager(TicketFileStore store, UserManager users) throws IOException {
        this.store = store;
        this.users = users;
        TicketFileStore.Contents contents = store.load(users::findByDni);
        this.tickets = new ArrayList<>(contents.tickets());
        this.historicalLines = new ArrayList<>(contents.historicalLines());
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public Optional<Ticket> find(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Result create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        if (!UserValidator.isNonBlank(id)) {
            return Result.error("Ticket identifier must not be empty.");
        }
        if (isIdTaken(id)) {
            return Result.error("A ticket with identifier " + id + " already exists.");
        }
        Optional<User> user = users.findByEmail(email);
        if (user.isEmpty()) {
            return Result.error("No registered user with email " + email + ".");
        }
        if (date.isBefore(today)) {
            return Result.error("Ticket date " + date + " is before the system date " + today + ".");
        }
        List<Ticket> candidate = new ArrayList<>(tickets);
        candidate.add(new Ticket(id, date, price, user.get()));
        Result saved = commit(candidate);
        return saved.success()
                ? Result.verbatim(true, "OK TICKET CREATED -> " + price.toPlainString())
                : saved;
    }

    public Result delete(String id, LocalDate today) {
        Optional<Ticket> ticket = find(id);
        if (ticket.isEmpty()) {
            return Result.error("Ticket " + id + " does not exist.");
        }
        if (!ticket.get().getDate().isAfter(today)) {
            return Result.error("Ticket " + id + " can only be deleted if its date is later than the system date " + today + ".");
        }
        List<Ticket> candidate = new ArrayList<>(tickets);
        candidate.remove(ticket.get());
        Result saved = commit(candidate);
        return saved.success() ? Result.ok("Ticket " + id + " deleted.") : saved;
    }

    /** Whether the user owns a ticket dated on or after {@code today}. */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        return tickets.stream().anyMatch(t -> t.getUser() == user && !t.getDate().isBefore(today));
    }

    private boolean isIdTaken(String id) {
        if (find(id).isPresent()) {
            return true;
        }
        String prefix = id + ";";
        return historicalLines.stream().anyMatch(l -> l.startsWith(prefix));
    }

    private Result commit(List<Ticket> candidate) {
        try {
            store.save(candidate, historicalLines);
        } catch (IOException e) {
            return Result.error("Could not save tickets: " + e.getMessage());
        }
        tickets = candidate;
        return Result.ok("Tickets saved.");
    }
}
