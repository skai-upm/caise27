package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.AttractionRepository;
import es.upm.amusementpark.repository.TicketRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Attraction management operations.
 */
public class AttractionService {

    private final AttractionRepository attractions;
    private final TicketRepository tickets;

    public AttractionService(AttractionRepository attractions) {
        this(attractions, new TicketRepository());
    }

    public AttractionService(AttractionRepository attractions, TicketRepository tickets) {
        this.attractions = attractions;
        this.tickets = tickets;
    }

    /**
     * Admits the users of a ticket valid today into an attraction, all or none.
     *
     * @return number of users admitted
     */
    public int enter(String attractionId, String ticketId) {
        Attraction attraction = findAttraction(attractionId);
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new OperationException("ticket '" + ticketId + "' not found"));
        LocalDate today = Park.getInstance().getCurrentDate();
        if (!ticket.isValidOn(today)) {
            throw new OperationException("ticket '" + ticketId + "' is valid on " + ticket.getDate()
                    + ", not on the current date " + today);
        }
        List<User> users = ticket.admittedUsers();
        if (users.stream().anyMatch(attraction::contains)) {
            throw new OperationException("a user of ticket '" + ticketId + "' is already inside " + attractionId);
        }
        if (!attraction.admitAll(users)) {
            throw new OperationException("not enough capacity in " + attractionId + " (" + attraction.availableCapacity()
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
            throw new OperationException("attraction identifier and name must not be empty");
        }
        if (attractions.findById(identifier).isPresent()) {
            throw new OperationException("an attraction with identifier '" + identifier + "' already exists");
        }
        Attraction attraction = new Attraction(identifier, name, parseCapacity(capacityText));
        attractions.add(attraction);
        return attraction;
    }

    public Attraction findAttraction(String identifier) {
        return attractions.findById(identifier)
                .orElseThrow(() -> new OperationException("attraction '" + identifier + "' not found"));
    }

    public void exit(String attractionId, String email) {
        Attraction attraction = findAttraction(attractionId);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new OperationException("user '" + email + "' is not inside " + attractionId));
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
            throw new OperationException("invalid capacity '" + text + "', expected a positive integer");
        }
        return value;
    }
}
