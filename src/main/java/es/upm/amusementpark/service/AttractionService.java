package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.AttractionRepository;

/**
 * Attraction management operations.
 */
public class AttractionService {

    private final AttractionRepository attractions;

    public AttractionService(AttractionRepository attractions) {
        this.attractions = attractions;
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
