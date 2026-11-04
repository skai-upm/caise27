package es.upm.park;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and looks up attractions. Definitions are persisted; occupancy lives in memory.
 */
public class AttractionManager {

    private final AttractionFileStorage storage;
    private Map<String, Attraction> attractions = new LinkedHashMap<>();

    public AttractionManager(AttractionFileStorage storage) {
        this.storage = storage;
        for (Attraction a : storage.load()) {
            attractions.put(a.getId(), a);
        }
    }

    public Collection<Attraction> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    public Attraction find(String id) {
        return attractions.get(id);
    }

    public Attraction create(String id, String name, String capacityText) {
        requireValid(UserValidator.isNonBlank(id), "invalid attraction identifier");
        requireValid(UserValidator.isNonBlank(name), "invalid attraction name");
        int capacity = parseCapacity(capacityText);
        requireValid(!attractions.containsKey(id), "an attraction with identifier " + id + " already exists");
        Attraction attraction = new Attraction(id, name, capacity);
        Map<String, Attraction> next = new LinkedHashMap<>(attractions);
        next.put(id, attraction);
        storage.save(next.values());
        attractions = next;
        return attraction;
    }

    /** Positive whole number written with digits only. */
    static int parseCapacity(String text) {
        String error = "invalid capacity '" + text + "', expected a positive whole number";
        boolean digits = !text.isEmpty();
        for (char c : text.toCharArray()) {
            digits &= c >= '0' && c <= '9';
        }
        requireValid(digits, error);
        try {
            int capacity = Integer.parseInt(text);
            requireValid(capacity > 0, error);
            return capacity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
