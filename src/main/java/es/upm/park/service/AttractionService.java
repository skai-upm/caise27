package es.upm.park.service;

import java.time.LocalDate;
import java.util.List;

import es.upm.park.model.Attraction;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.repository.AttractionRepository;

/**
 * Operations on attractions.
 */
public class AttractionService {

    private static final int MAX_CAPACITY_DIGITS = 9;

    private final AttractionRepository attractions;

    public AttractionService(AttractionRepository attractions) {
        this.attractions = attractions;
    }

    public Attraction create(String id, String name, String capacity) {
        if (id == null || id.isBlank()) {
            throw new AttractionException("attraction identifier is required");
        }
        if (attractions.findById(id).isPresent()) {
            throw new AttractionException("attraction " + id + " already exists");
        }
        if (name == null || name.isBlank()) {
            throw new AttractionException("attraction name is required");
        }
        Attraction attraction = new Attraction(id, name, parseCapacity(capacity));
        attractions.add(attraction);
        return attraction;
    }

    public Attraction find(String id) {
        return attractions.findById(id).orElseThrow(() -> new AttractionException("no attraction " + id));
    }

    /** Emails of the users currently inside; read-only. */
    public List<String> occupantEmails(String id) {
        return find(id).getOccupants().stream().map(User::getEmail).toList();
    }

    /**
     * Admits every user of the ticket into the attraction, or nobody: the
     * ticket must be valid today, none of its users may already be inside and
     * the attraction must have room for all of them.
     *
     * @return the users who entered
     */
    public List<User> enter(String attractionId, Ticket ticket, LocalDate today) {
        Attraction attraction = find(attractionId);
        if (!ticket.isValidOn(today)) {
            throw new AttractionException("ticket " + ticket.getId() + " is not valid on " + today);
        }
        List<User> holders = ticket.holders();
        for (User user : holders) {
            if (attraction.contains(user)) {
                throw new AttractionException("user " + user.getEmail() + " is already inside attraction "
                        + attractionId);
            }
        }
        if (attraction.getOccupants().size() + holders.size() > attraction.getCapacity()) {
            throw new AttractionException("attraction " + attractionId + " does not have capacity for "
                    + holders.size() + " users");
        }
        holders.forEach(attraction::enter);
        return holders;
    }

    private static int parseCapacity(String text) {
        boolean digits = text != null && !text.isEmpty() && text.length() <= MAX_CAPACITY_DIGITS
                && text.chars().allMatch(c -> c >= '0' && c <= '9');
        int value = digits ? Integer.parseInt(text) : 0;
        if (value <= 0) {
            throw new AttractionException("invalid capacity " + text);
        }
        return value;
    }
}
