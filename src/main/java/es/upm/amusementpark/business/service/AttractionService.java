package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.RuleViolationException;


import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Attraction management operations.
 */
public class AttractionService {

    private AttractionRepository attractions;
    private TicketRepository tickets;

    private AttractionService() {
    }

    private static final class Holder {
        private static final AttractionService INSTANCE = new AttractionService();
    }

    /** The single instance of this controller. */
    public static AttractionService getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the repositories this controller works with (done during application initialization). */
    public AttractionService inject(AttractionRepository attractions, TicketRepository tickets) {
        this.attractions = attractions;
        this.tickets = tickets;
        return this;
    }



    /**
     * Admits the users of a ticket valid today into an attraction, all or none.
     *
     * @return number of users admitted
     */
    public int enter(String attractionId, String ticketId) {
        Attraction attraction = findAttraction(attractionId);
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("ticket '" + ticketId + "' not found"));
        LocalDate today = Park.getInstance().getCurrentDate();
        if (!ticket.isValidOn(today)) {
            throw new RuleViolationException("ticket '" + ticketId + "' is valid on " + ticket.getDate()
                    + ", not on the current date " + today);
        }
        List<User> users = ticket.admittedUsers();
        if (users.stream().anyMatch(attraction::contains)) {
            throw new RuleViolationException("a user of ticket '" + ticketId + "' is already inside " + attractionId);
        }
        if (!attraction.admitAll(users)) {
            throw new RuleViolationException("not enough capacity in " + attractionId + " (" + attraction.availableCapacity()
                    + " available, " + users.size() + " needed)");
        }
        attractions.update(attraction);
        return users.size();
    }

    public AttractionRepository attractions() {
        return attractions;
    }

    public Attraction createAttraction(String identifier, String name, String capacityText) {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new InvalidDataException("attraction identifier and name must not be empty");
        }
        if (attractions.findById(identifier).isPresent()) {
            throw new RuleViolationException("an attraction with identifier '" + identifier + "' already exists");
        }
        Attraction attraction = new Attraction(identifier, name, parseCapacity(capacityText));
        attractions.add(attraction);
        return attraction;
    }

    public Attraction findAttraction(String identifier) {
        return attractions.findById(identifier)
                .orElseThrow(() -> new NotFoundException("attraction '" + identifier + "' not found"));
    }

    public void exit(String attractionId, String email) {
        Attraction attraction = findAttraction(attractionId);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new NotFoundException("user '" + email + "' is not inside " + attractionId));
        attraction.remove(user);
        attractions.update(attraction);
    }

    /** Read-only status of an attraction, from its own state. */
    public String status(String identifier) {
        Attraction a = findAttraction(identifier);
        String users = a.getOccupants().isEmpty() ? "none"
                : String.join(", ", a.getOccupants().stream().map(User::getEmail).toList());
        return String.join(System.lineSeparator(),
                "ATTRACTION " + a.getIdentifier() + " - " + a.getName(),
                "Capacity: " + a.getCapacity(),
                "Inside: " + a.getOccupants().size(),
                "Available: " + a.availableCapacity(),
                "Users: " + users);
    }

    /** Read-only listing of the users currently inside an attraction. */
    public String list(String identifier) {
        Attraction a = findAttraction(identifier);
        StringBuilder sb = new StringBuilder("ATTRACTION " + a.getIdentifier() + " (" + a.getName() + ") "
                + a.getOccupants().size() + "/" + a.getCapacity() + ":");
        if (a.getOccupants().isEmpty()) {
            return sb.append(" no users").toString();
        }
        for (User u : a.getOccupants()) {
            sb.append(System.lineSeparator()).append(u.getEmail());
        }
        return sb.toString();
    }

    private static int parseCapacity(String text) {
        boolean digits = !text.isEmpty() && text.length() <= 9;
        for (char c : text.toCharArray()) {
            digits &= c >= '0' && c <= '9';
        }
        int value = digits ? Integer.parseInt(text) : 0;
        if (value <= 0) {
            throw new InvalidDataException("invalid capacity '" + text + "', expected a positive integer");
        }
        return value;
    }
}
