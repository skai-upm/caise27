package com.amusementpark.business.controller;

import com.amusementpark.persistence.AttractionRepository;
import com.amusementpark.business.model.Attraction;
import com.amusementpark.business.model.AttractionType;
import com.amusementpark.business.model.User;
import com.amusementpark.business.model.UserValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Attraction definitions (persisted) and their current visitors (runtime only).
 */
public class AttractionController {

    private static final AttractionController INSTANCE = new AttractionController();

    private AttractionRepository storage;
    private final List<Attraction<?>> attractions = new ArrayList<>();

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and loads the attraction definitions; visitors start empty. */
    public void configure(AttractionRepository storage) throws IOException {
        this.storage = storage;
        attractions.clear();
        attractions.addAll(storage.load());
    }

    public List<Attraction<?>> getAttractions() {
        return new ArrayList<>(attractions);
    }

    public Attraction<?> find(String id) {
        for (Attraction<?> a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    /** Creates an attraction; a {@code null} type text means GENERAL (E2 syntax). */
    public String create(String id, String name, String capacityText, String typeText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR: Attraction identifier and name must not be empty.";
        }
        if (find(id) != null) {
            return "ERROR: Attraction " + id + " already exists.";
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            capacity = 0;
        }
        if (capacity <= 0) {
            return "ERROR: Capacity must be a positive integer: " + capacityText;
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromText(typeText);
        if (type == null) {
            return "ERROR: Unknown attraction type (GENERAL, INDIVIDUAL, GROUP, SPECIAL): " + typeText;
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        try {
            storage.save(attractions);
        } catch (IOException e) {
            attractions.remove(attraction);
            return "ERROR: Could not save attractions file.";
        }
        return "OK: Attraction created: " + id + " (" + type + ")";
    }

    /** Multi-line description of the attraction's current state. */
    public static String status(Attraction<?> a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Attraction ").append(a.getId()).append(" - ").append(a.getName())
                .append(" [").append(a.getType()).append(']').append(System.lineSeparator());
        sb.append("  Capacity: ").append(a.getCapacity())
                .append(" | Present: ").append(a.getVisitors().size())
                .append(" | Available: ").append(a.getAvailable()).append(System.lineSeparator());
        sb.append("  Users present:");
        if (a.getVisitors().isEmpty()) {
            sb.append(" none");
        }
        for (User u : a.getVisitors()) {
            sb.append(System.lineSeparator()).append("    ").append(u.getName()).append(' ')
                    .append(u.getSurname()).append(" (").append(u.getEmail()).append(')');
        }
        return sb.toString();
    }
}
