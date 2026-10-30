package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.AccessDeniedException;
import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.AttractionRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Attraction operations. Access is handled through the {@link AccessTicket} capabilities; each attraction type
 * decides which kinds of ticket it admits.
 */
public class AttractionController {

    private static AttractionController instance;

    private AttractionRepository repository;
    private List<Attraction> attractions = new ArrayList<>();

    private AttractionController() {
    }

    /** Returns the unique instance of this controller. */
    public static synchronized AttractionController getInstance() {
        if (instance == null) {
            instance = new AttractionController();
        }
        return instance;
    }

    /** Injects the repository and loads the stored attraction definitions. */
    public void configure(AttractionRepository repository) throws IOException {
        this.repository = repository;
        this.attractions = new ArrayList<>(repository.load());
    }

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    public Attraction findById(String id) {
        for (Attraction a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public String create(String id, String name, String capacityText, String typeText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR: attraction identifier and name must not be empty";
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            capacity = 0;
        }
        if (capacity <= 0) {
            return "ERROR: capacity must be a positive integer";
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.parse(typeText);
        if (type == null) {
            return "ERROR: attraction type must be GENERAL, INDIVIDUAL, GROUP or SPECIAL";
        }
        if (findById(id) != null) {
            return "ERROR: attraction " + id + " already exists";
        }
        Attraction attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        try {
            repository.save(attractions);
        } catch (IOException e) {
            attractions.remove(attraction);
            return "ERROR: could not save attractions";
        }
        return "OK: " + type + " attraction " + id + " created";
    }

    public String enter(Attraction attraction, AccessTicket ticket, boolean speed, LocalDate today) {
        List<Pass> consumed;
        try {
            consumed = attraction.enter(ticket, speed, today);
        } catch (AccessDeniedException e) {
            return "ERROR: " + e.getMessage();
        }
        StringBuilder sb = new StringBuilder("OK: ").append(ticket.getUsers().size()).append(" user(s) entered ")
                .append(attraction.getId());
        for (Pass pass : consumed) {
            sb.append("; consumed ").append(pass.getKind()).append(' ').append(pass.getId());
        }
        return sb.toString();
    }

    public String exit(Attraction attraction, AccessTicket ticket) {
        int count = attraction.exitAll(ticket.getUsers());
        if (count == 0) {
            return "ERROR: no user of ticket " + ticket.getId() + " is in " + attraction.getId();
        }
        return "OK: " + count + " user(s) left " + attraction.getId();
    }

    public String status(Attraction attraction) {
        StringBuilder sb = new StringBuilder();
        sb.append("Attraction: ").append(attraction.getId()).append(" - ").append(attraction.getName()).append('\n');
        sb.append("Type: ").append(attraction.getType()).append('\n');
        sb.append("Capacity: ").append(attraction.getCapacity()).append('\n');
        sb.append("Occupancy: ").append(attraction.getPresent().size()).append('\n');
        sb.append("Available: ").append(attraction.getAvailable()).append('\n');
        sb.append("Users present:");
        if (attraction.getPresent().isEmpty()) {
            sb.append(" none");
        }
        for (User u : attraction.getPresent()) {
            sb.append("\n  - ").append(u.getName()).append(' ').append(u.getSurname()).append(" (")
                    .append(u.getEmail()).append(')');
        }
        return sb.toString();
    }
}
