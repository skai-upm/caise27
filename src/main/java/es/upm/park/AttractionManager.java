package es.upm.park;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Creates attractions and handles ticket-based entry, exit and status queries.
 */
public class AttractionManager {

    private static final int MAX_CAPACITY_DIGITS = 9;

    private final AttractionFileStore store;
    private List<Attraction> attractions;

    public AttractionManager(AttractionFileStore store) throws IOException {
        this.store = store;
        this.attractions = new ArrayList<>(store.load());
    }

    public List<Attraction> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Optional<Attraction> find(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    /** Positive whole number written with ASCII digits, or {@code null}. */
    static Integer parseCapacity(String text) {
        if (text.isEmpty() || text.length() > MAX_CAPACITY_DIGITS) {
            return null;
        }
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return null;
            }
        }
        int value = Integer.parseInt(text);
        return value > 0 ? value : null;
    }

    public Result create(String id, String name, String capacityText) {
        if (!UserValidator.isNonBlank(id) || !UserValidator.isNonBlank(name)) {
            return Result.error("Attraction identifier and name must not be empty.");
        }
        Integer capacity = parseCapacity(capacityText);
        if (capacity == null) {
            return Result.error("Invalid capacity '" + capacityText + "': expected a positive whole number.");
        }
        if (find(id).isPresent()) {
            return Result.error("An attraction with identifier " + id + " already exists.");
        }
        List<Attraction> candidate = new ArrayList<>(attractions);
        candidate.add(new Attraction(id, name, capacity));
        try {
            store.save(candidate);
        } catch (IOException e) {
            return Result.error("Could not save attractions: " + e.getMessage());
        }
        attractions = candidate;
        return Result.ok("Attraction " + id + " created.");
    }

    public Result enter(String attractionId, Optional<Ticket> ticket, String ticketId, LocalDate today) {
        Optional<Attraction> attraction = find(attractionId);
        if (attraction.isEmpty()) {
            return Result.error("Attraction " + attractionId + " does not exist.");
        }
        if (ticket.isEmpty()) {
            return Result.error("Ticket " + ticketId + " does not exist.");
        }
        return attraction.get().enter(ticket.get(), today);
    }

    public Result exit(String attractionId, Optional<Ticket> ticket, String ticketId) {
        Optional<Attraction> attraction = find(attractionId);
        if (attraction.isEmpty()) {
            return Result.error("Attraction " + attractionId + " does not exist.");
        }
        if (ticket.isEmpty()) {
            return Result.error("Ticket " + ticketId + " does not exist.");
        }
        return attraction.get().exit(ticket.get());
    }

    public Result status(String attractionId) {
        Optional<Attraction> found = find(attractionId);
        if (found.isEmpty()) {
            return Result.error("Attraction " + attractionId + " does not exist.");
        }
        Attraction a = found.get();
        String users = a.getPresent().isEmpty() ? "(none)" : a.getPresent().stream()
                .map(u -> u.getFirstName() + " " + u.getSurnames() + " (" + u.getEmail() + ")")
                .collect(Collectors.joining(", "));
        String nl = System.lineSeparator();
        return Result.ok("Attraction " + a.getId() + " - " + a.getName() + nl
                + "Capacity: " + a.getCapacity() + nl
                + "Present: " + a.getPresent().size() + nl
                + "Available: " + a.getAvailable() + nl
                + "Users present: " + users);
    }

    public void removeUserEverywhere(User user) {
        attractions.forEach(a -> a.remove(user));
    }
}
