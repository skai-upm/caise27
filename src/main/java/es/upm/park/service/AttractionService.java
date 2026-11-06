package es.upm.park.service;

import java.util.List;

import es.upm.park.model.Attraction;
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
