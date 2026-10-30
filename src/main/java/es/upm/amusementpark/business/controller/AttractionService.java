package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.AttractionRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Attraction operations. Access is handled through the {@link Ticket} abstraction, so individual and group
 * tickets share the same entry and exit logic.
 */
public class AttractionService {

    private final AttractionRepository repository;
    private final List<Attraction> attractions;

    public AttractionService(AttractionRepository repository) throws IOException {
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

    public String create(String id, String name, String capacityText) {
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
        if (findById(id) != null) {
            return "ERROR: attraction " + id + " already exists";
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        try {
            repository.save(attractions);
        } catch (IOException e) {
            attractions.remove(attraction);
            return "ERROR: could not save attractions";
        }
        return "OK: attraction " + id + " created";
    }

    public String enter(Attraction attraction, Ticket ticket, LocalDate today) {
        if (!ticket.isValidOn(today)) {
            return "ERROR: ticket " + ticket.getId() + " is not valid today (" + today + ")";
        }
        List<User> users = ticket.getUsers();
        if (users.size() > attraction.getAvailable()) {
            return "ERROR: not enough places in " + attraction.getId() + " (" + attraction.getAvailable()
                    + " available, " + users.size() + " requested)";
        }
        if (!attraction.enterAll(users)) {
            return "ERROR: some users of ticket " + ticket.getId() + " are already in " + attraction.getId();
        }
        return "OK: " + users.size() + " user(s) entered " + attraction.getId();
    }

    public String exit(Attraction attraction, Ticket ticket) {
        int count = attraction.exitAll(ticket.getUsers());
        if (count == 0) {
            return "ERROR: no user of ticket " + ticket.getId() + " is in " + attraction.getId();
        }
        return "OK: " + count + " user(s) left " + attraction.getId();
    }

    public String status(Attraction attraction) {
        StringBuilder sb = new StringBuilder();
        sb.append("Attraction: ").append(attraction.getId()).append(" - ").append(attraction.getName()).append('\n');
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
