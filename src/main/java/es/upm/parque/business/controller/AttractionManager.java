package es.upm.parque.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.AttractionRepository;

/**
 * Attraction management and visitor access. Individual and group tickets are handled uniformly
 * through the users each ticket admits.
 */
public class AttractionManager {

    public static final String ATTRACTION_CREATED = "OK ATTRACTION CREATED";
    public static final String ATTRACTION_DELETED = "OK ATTRACTION DELETED";
    public static final String DUPLICATE_ATTRACTION = "ERROR DUPLICATE ATTRACTION";
    public static final String ATTRACTION_NOT_FOUND = "ERROR ATTRACTION NOT FOUND";
    public static final String NOT_EMPTY = "ERROR ATTRACTION NOT EMPTY";
    public static final String INVALID_CAPACITY = "ERROR INVALID CAPACITY";
    public static final String NOT_VALID_TODAY = "ERROR TICKET NOT VALID TODAY";
    public static final String ALREADY_INSIDE = "ERROR USER ALREADY INSIDE";
    public static final String CAPACITY_EXCEEDED = "ERROR CAPACITY EXCEEDED";
    public static final String NOT_INSIDE = "ERROR NOT INSIDE";

    private final AttractionRepository repository;
    private final TicketManager tickets;
    private final SystemDate systemDate;
    private final Map<String, Attraction> attractionsById = new LinkedHashMap<>();

    public AttractionManager(AttractionRepository repository, TicketManager tickets, SystemDate systemDate)
            throws IOException {
        this.repository = repository;
        this.tickets = tickets;
        this.systemDate = systemDate;
        for (Attraction attraction : repository.load()) {
            attractionsById.put(attraction.getId(), attraction);
        }
    }

    public String create(String id, String name, String capacityText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return UserManager.EMPTY_FIELD;
        }
        Integer capacity = Attraction.parseCapacity(capacityText);
        if (capacity == null) {
            return INVALID_CAPACITY;
        }
        if (attractionsById.containsKey(id)) {
            return DUPLICATE_ATTRACTION;
        }
        attractionsById.put(id, new Attraction(id, name, capacity));
        return persist(ATTRACTION_CREATED);
    }

    public String delete(String id) {
        Attraction attraction = attractionsById.get(id);
        if (attraction == null) {
            return ATTRACTION_NOT_FOUND;
        }
        if (!attraction.isEmpty()) {
            return NOT_EMPTY;
        }
        attractionsById.remove(id);
        return persist(ATTRACTION_DELETED);
    }

    public String enter(String attractionId, String ticketId) {
        Attraction attraction = attractionsById.get(attractionId);
        if (attraction == null) {
            return ATTRACTION_NOT_FOUND;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return TicketManager.TICKET_NOT_FOUND;
        }
        if (!ticket.getDate().equals(systemDate.get())) {
            return NOT_VALID_TODAY;
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.stream().anyMatch(this::isInsideAnyAttraction)) {
            return ALREADY_INSIDE;
        }
        if (!attraction.canAdmit(visitors)) {
            return CAPACITY_EXCEEDED;
        }
        attraction.admit(visitors);
        return "OK ENTERED " + visitors.size() + " USERS";
    }

    public String exit(String attractionId, String ticketId) {
        Attraction attraction = attractionsById.get(attractionId);
        if (attraction == null) {
            return ATTRACTION_NOT_FOUND;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return TicketManager.TICKET_NOT_FOUND;
        }
        int released = attraction.release(ticket.getVisitors());
        return released == 0 ? NOT_INSIDE : "OK EXITED " + released + " USERS";
    }

    public String status(String attractionId) {
        Attraction attraction = attractionsById.get(attractionId);
        if (attraction == null) {
            return ATTRACTION_NOT_FOUND;
        }
        List<String> lines = new ArrayList<>();
        lines.add("ATTRACTION " + attraction.getId() + " - " + attraction.getName());
        lines.add("Capacity: " + attraction.getCapacity() + " | Present: " + attraction.getPresent().size()
                + " | Available: " + attraction.available());
        lines.add("Users present:");
        for (User user : attraction.getPresent()) {
            lines.add("- " + user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")");
        }
        if (attraction.isEmpty()) {
            lines.add("- none");
        }
        return String.join(System.lineSeparator(), lines);
    }

    public Attraction find(String id) {
        return attractionsById.get(id);
    }

    private boolean isInsideAnyAttraction(User user) {
        return attractionsById.values().stream().anyMatch(a -> a.contains(user));
    }

    private String persist(String success) {
        try {
            repository.save(attractionsById.values());
            return success;
        } catch (IOException e) {
            return UserManager.STORAGE_FAILURE;
        }
    }
}
