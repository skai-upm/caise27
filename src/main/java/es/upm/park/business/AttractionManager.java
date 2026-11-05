package es.upm.park.business;

import es.upm.park.business.model.Result;
import es.upm.park.business.model.AccessCredential;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
import es.upm.park.persistence.AttractionRepository;
import es.upm.park.business.model.User;
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
public final class AttractionManager {

    private static final AttractionManager INSTANCE = new AttractionManager();

    private AttractionRepository store;
    private List<Attraction<?>> attractions = new ArrayList<>();

    private AttractionManager() {
    }

    /** The single attraction manager of the execution. */
    public static AttractionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository into the single instance and loads the attraction definitions
     * (all empty). Called at startup by {@link ApplicationContext}.
     */
    public static AttractionManager configure(AttractionRepository store) throws IOException {
        List<Attraction<?>> loaded = new ArrayList<>(store.load());
        INSTANCE.store = store;
        INSTANCE.attractions = loaded;
        return INSTANCE;
    }

    public List<Attraction<?>> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Optional<Attraction<?>> find(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    /** Creates a GENERAL attraction. */
    public Result create(String id, String name, String capacityText) {
        return create(id, name, capacityText, AttractionType.GENERAL);
    }

    public Result create(String id, String name, String capacityText, AttractionType type) {
        if (!UserValidator.isNonBlank(id) || !UserValidator.isNonBlank(name)) {
            return Result.error("Attraction identifier and name must not be empty.");
        }
        Integer capacity = Attraction.parseCapacity(capacityText);
        if (capacity == null) {
            return Result.error("Invalid capacity '" + capacityText + "': expected a positive whole number.");
        }
        if (find(id).isPresent()) {
            return Result.error("An attraction with identifier " + id + " already exists.");
        }
        List<Attraction<?>> candidate = new ArrayList<>(attractions);
        candidate.add(type.create(id, name, capacity));
        try {
            store.save(candidate);
        } catch (IOException e) {
            return Result.error("Could not save attractions: " + e.getMessage());
        }
        attractions = candidate;
        return Result.ok("Attraction " + id + " created.");
    }

    public Result enter(String attractionId, Optional<? extends AccessCredential> ticket, String ticketId, LocalDate today) {
        Optional<Attraction<?>> attraction = find(attractionId);
        if (attraction.isEmpty()) {
            return Result.error("Attraction " + attractionId + " does not exist.");
        }
        if (ticket.isEmpty()) {
            return Result.error("Ticket " + ticketId + " does not exist.");
        }
        return attraction.get().enter(ticket.get(), today);
    }

    public Result exit(String attractionId, Optional<? extends AccessCredential> ticket, String ticketId) {
        Optional<Attraction<?>> attraction = find(attractionId);
        if (attraction.isEmpty()) {
            return Result.error("Attraction " + attractionId + " does not exist.");
        }
        if (ticket.isEmpty()) {
            return Result.error("Ticket " + ticketId + " does not exist.");
        }
        return attraction.get().exit(ticket.get());
    }

    public Result status(String attractionId) {
        Optional<Attraction<?>> found = find(attractionId);
        if (found.isEmpty()) {
            return Result.error("Attraction " + attractionId + " does not exist.");
        }
        Attraction<?> a = found.get();
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
