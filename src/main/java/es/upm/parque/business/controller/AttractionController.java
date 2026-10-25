package es.upm.parque.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.AccessCapability;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.EntryResult;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.validation.UserValidator;
import es.upm.parque.persistence.AttractionRepository;

/**
 * Manages attractions and access to them (Singleton).
 */
public final class AttractionController {

    public static final String SPEED_OPTION = "SPEED";

    private static AttractionController instance;

    private final List<Attraction<?>> attractions = new ArrayList<>();
    private AttractionRepository repository;
    private ParkController park;
    private TicketController tickets;

    private AttractionController() {
    }

    public static synchronized AttractionController getInstance() {
        if (instance == null) {
            instance = new AttractionController();
        }
        return instance;
    }

    public void configure(AttractionRepository repository, ParkController park, TicketController tickets) {
        this.repository = repository;
        this.park = park;
        this.tickets = tickets;
    }

    /**
     * Loads the stored attractions; they always start empty.
     */
    public void load() throws IOException {
        attractions.clear();
        attractions.addAll(repository.load());
    }

    public List<Attraction<?>> getAttractions() {
        return List.copyOf(attractions);
    }

    public Attraction<?> find(String id) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    /**
     * Creates an attraction; without a type it is GENERAL.
     */
    public String createAttraction(String id, String name, String capacityText, String typeName) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR Attraction identifier and name must not be empty";
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            return "ERROR Invalid capacity: " + capacityText;
        }
        if (capacity <= 0) {
            return "ERROR Capacity must be positive";
        }
        AttractionType type = typeName == null ? AttractionType.GENERAL : AttractionType.fromName(typeName);
        if (type == null) {
            return "ERROR Unknown attraction type: " + typeName + ". Types: GENERAL, INDIVIDUAL, GROUP, SPECIAL";
        }
        if (find(id) != null) {
            return "ERROR Attraction " + id + " already exists";
        }
        attractions.add(type.create(id, name, capacity));
        return persist() ? "OK ATTRACTION CREATED " + id : "ERROR Could not save attractions";
    }

    public String deleteAttraction(String id) {
        Attraction<?> attraction = find(id);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + id;
        }
        if (!attraction.getPresent().isEmpty()) {
            return "ERROR Attraction " + id + " has users inside";
        }
        attractions.remove(attraction);
        return persist() ? "OK ATTRACTION DELETED " + id : "ERROR Could not save attractions";
    }

    /**
     * Lets the ticket's users into the attraction. {@code option} is {@code null} or {@code SPEED}.
     */
    public String enterAttraction(String attractionId, String ticketId, String option) {
        if (option != null && !SPEED_OPTION.equals(option)) {
            return "ERROR Unknown entry option: " + option + " (only SPEED is accepted)";
        }
        Attraction<?> attraction = find(attractionId);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + attractionId;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return "ERROR Unknown ticket: " + ticketId;
        }
        if (!ticket.isValidOn(park.getCurrentDate())) {
            return "ERROR Ticket " + ticketId + " is for " + ticket.getDate() + ", not for " + park.getCurrentDate();
        }
        EntryResult result;
        try {
            result = enter(attraction, ticket, option != null);
        } catch (IllegalStateException e) {
            return "ERROR " + e.getMessage();
        }
        if (!result.consumedPasses().isEmpty() && !tickets.save()) {
            return "ERROR Entry done but the consumed passes could not be saved";
        }
        return "OK ENTERED " + attractionId + " -> " + result.users() + " user(s)" + describe(result);
    }

    private static <T extends AccessCapability> EntryResult enter(Attraction<T> attraction, AccessCapability access,
            boolean speed) {
        T admitted = attraction.admit(access);
        return attraction.enter(admitted, speed);
    }

    private static String describe(EntryResult result) {
        List<String> speedPasses = new ArrayList<>();
        List<String> specialPasses = new ArrayList<>();
        for (Pass pass : result.consumedPasses()) {
            (pass instanceof SpecialPass ? specialPasses : speedPasses).add(pass.getId());
        }
        StringBuilder text = new StringBuilder();
        if (!speedPasses.isEmpty()) {
            text.append("; SpeedPass consumed: ").append(String.join(", ", speedPasses));
        }
        if (!specialPasses.isEmpty()) {
            text.append("; Special Pass consumed: ").append(String.join(", ", specialPasses));
        }
        return text.toString();
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction<?> attraction = find(attractionId);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + attractionId;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return "ERROR Unknown ticket: " + ticketId;
        }
        try {
            int left = attraction.exit(ticket);
            return "OK EXITED " + attractionId + " -> " + left + " user(s)";
        } catch (IllegalStateException e) {
            return "ERROR " + e.getMessage();
        }
    }

    public String attractionStatus(String attractionId) {
        Attraction<?> attraction = find(attractionId);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + attractionId;
        }
        List<String> present = new ArrayList<>();
        for (User user : attraction.getPresent()) {
            present.add(user.getName() + " " + user.getSurname() + " <" + user.getEmail() + ">");
        }
        return String.join(System.lineSeparator(),
                "OK ATTRACTION STATUS " + attraction.getId(),
                "  Name: " + attraction.getName(),
                "  Type: " + attraction.getType(),
                "  Maximum capacity: " + attraction.getCapacity(),
                "  Users present: " + attraction.getPresent().size(),
                "  Available places: " + attraction.getAvailablePlaces(),
                "  Present: " + (present.isEmpty() ? "(none)" : String.join(", ", present)));
    }

    private boolean persist() {
        try {
            repository.saveAll(attractions);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
