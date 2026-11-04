package com.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Attraction definitions (persisted) and their live occupancy (memory only).
 */
public class AttractionService {
    private final AttractionRepository repository;
    private List<Attraction> attractions;

    public AttractionService(AttractionRepository repository) throws IOException {
        this.repository = repository;
        this.attractions = new ArrayList<>(repository.load());
    }

    public List<Attraction> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction find(String id) {
        for (Attraction a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    /** Finds an attraction or rejects the operation. */
    public Attraction existing(String id) {
        Attraction a = find(id);
        if (a == null) {
            throw new IllegalArgumentException("No attraction with identifier " + id);
        }
        return a;
    }

    public Attraction create(String id, String name, String capacityText) {
        require(UserValidator.isNonBlank(id), "Attraction identifier must not be empty");
        require(UserValidator.isNonBlank(name), "Attraction name must not be empty");
        require(find(id) == null, "An attraction with identifier " + id + " already exists");
        int capacity = parseCapacity(capacityText);
        Attraction attraction = new Attraction(id, name, capacity);
        List<Attraction> next = new ArrayList<>(attractions);
        next.add(attraction);
        try {
            repository.save(next);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save attractions: " + e.getMessage(), e);
        }
        attractions = next;
        return attraction;
    }

    private static int parseCapacity(String text) {
        boolean digits = !text.isEmpty() && text.length() <= 9;
        for (int i = 0; digits && i < text.length(); i++) {
            digits = text.charAt(i) >= '0' && text.charAt(i) <= '9';
        }
        require(digits && Integer.parseInt(text) > 0,
                "Invalid capacity: '" + text + "' (expected a positive whole number)");
        return Integer.parseInt(text);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
