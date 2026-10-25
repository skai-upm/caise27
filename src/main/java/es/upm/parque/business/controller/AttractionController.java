package es.upm.parque.business.controller;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.persistence.AttractionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the park attractions and persists their definitions.
 * Single instance (Singleton); its dependencies are injected with {@code configure} at startup.
 */
public class AttractionController {

    private AttractionRepository repository;
    private final List<Attraction<?>> attractions = new ArrayList<>();

    private static final AttractionController INSTANCE = new AttractionController();

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and loads the stored attractions (always empty of users).
     */
    public void configure(AttractionRepository repository) throws IOException {
        this.repository = repository;
        attractions.clear();
        attractions.addAll(repository.load());
    }

    public List<Attraction<?>> getAttractions() {
        return List.copyOf(attractions);
    }

    public Attraction<?> find(String id) {
        for (Attraction<?> a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public String create(String id, String name, String capacityText, String typeText) throws IOException {
        if (id.isBlank() || name.isBlank()) {
            return "ERROR attraction id and name are required";
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            return "ERROR invalid capacity: " + capacityText;
        }
        if (capacity <= 0) {
            return "ERROR capacity must be positive";
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromString(typeText);
        if (type == null) {
            return "ERROR invalid attraction type: " + typeText + " (GENERAL, INDIVIDUAL, GROUP, SPECIAL)";
        }
        if (find(id) != null) {
            return "ERROR attraction " + id + " already exists";
        }
        attractions.add(type.create(id, name, capacity));
        repository.save(attractions);
        return "OK ATTRACTION CREATED (" + type + ")";
    }

    public String delete(String id) throws IOException {
        Attraction<?> a = find(id);
        if (a == null) {
            return "ERROR attraction " + id + " does not exist";
        }
        if (!a.getUsers().isEmpty()) {
            return "ERROR attraction " + id + " is not empty";
        }
        attractions.remove(a);
        repository.save(attractions);
        return "OK ATTRACTION DELETED";
    }
}
