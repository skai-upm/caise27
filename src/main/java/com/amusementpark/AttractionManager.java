package com.amusementpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Attraction definitions (persisted) and their current visitors (runtime only).
 */
public class AttractionManager {

    private final AttractionFileStorage storage;
    private final List<Attraction> attractions;

    public AttractionManager(AttractionFileStorage storage) throws IOException {
        this.storage = storage;
        this.attractions = storage.load();
    }

    public List<Attraction> getAttractions() {
        return new ArrayList<>(attractions);
    }

    public Attraction find(String id) {
        for (Attraction a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public String create(String id, String name, String capacityText) {
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
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        try {
            storage.save(attractions);
        } catch (IOException e) {
            attractions.remove(attraction);
            return "ERROR: Could not save attractions file.";
        }
        return "OK: Attraction created: " + id;
    }

    /** Multi-line description of the attraction's current state. */
    public static String status(Attraction a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Attraction ").append(a.getId()).append(" - ").append(a.getName()).append(System.lineSeparator());
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
