package es.upm.parque.business.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.EntryResult;
import es.upm.parque.business.repository.AttractionRepository;
import es.upm.parque.business.validation.Identifiers;
import es.upm.parque.business.validation.UserValidator;

/**
 * Park attractions, entries and exits. Singleton.
 */
public final class AttractionController {

    private static final AttractionController INSTANCE = new AttractionController();
    private static final String SPEED_OPTION = "SPEED";

    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private AttractionRepository repository;
    private ParkController park;
    private TicketController tickets;
    private PassController passes;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(AttractionRepository repository, ParkController park, TicketController tickets,
                          PassController passes) {
        this.repository = repository;
        this.park = park;
        this.tickets = tickets;
        this.passes = passes;
    }

    public void load() {
        attractions.clear();
        for (Attraction<?> a : repository.load()) {
            attractions.put(a.getId(), a);
        }
    }

    public List<Attraction<?>> getAttractions() {
        return List.copyOf(attractions.values());
    }

    public Attraction<?> get(String id) {
        Attraction<?> attraction = attractions.get(id);
        if (attraction == null) {
            throw new IllegalArgumentException("Attraction not found: " + id);
        }
        return attraction;
    }

    /**
     * @param typeText attraction type, or null for GENERAL
     */
    public Attraction<?> create(String id, String name, String capacityText, String typeText) {
        Identifiers.require(id, "attraction");
        if (!UserValidator.isNotEmpty(name)) {
            throw new IllegalArgumentException("Empty attraction name");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            capacity = 0;
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer: " + capacityText);
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromText(typeText);
        if (attractions.containsKey(id)) {
            throw new IllegalArgumentException("Attraction already exists: " + id);
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.put(id, attraction);
        save();
        return attraction;
    }

    public void delete(String id) {
        Attraction<?> attraction = get(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new IllegalArgumentException("Attraction " + id + " has users inside");
        }
        attractions.remove(id);
        save();
    }

    /**
     * @param option null for a normal entry, or {@code SPEED} to use SpeedPasses
     */
    public EntryResult enter(String attractionId, String ticketId, String option) {
        Attraction<?> attraction = get(attractionId);
        boolean speed = option != null;
        if (speed && !SPEED_OPTION.equalsIgnoreCase(option)) {
            throw new IllegalArgumentException("Unknown entry option: " + option + " (only SPEED)");
        }
        EntryResult result = attraction.enter(tickets.get(ticketId), speed, park.getSystemDate());
        if (!result.consumed().isEmpty()) {
            passes.save();
        }
        return result;
    }

    public int exit(String attractionId, String ticketId) {
        return get(attractionId).exit(tickets.get(ticketId));
    }

    private void save() {
        repository.save(attractions.values());
    }
}
