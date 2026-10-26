package es.upm.parque.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.Admission;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.EntryCheck;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.PersonalPass;
import es.upm.parque.business.model.SpeedPass;
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
    public static final String INVALID_TYPE = "ERROR INVALID ATTRACTION TYPE";
    public static final String TYPE_NOT_ACCEPTED = "ERROR TICKET TYPE NOT ACCEPTED";
    public static final String SPEED_NOT_ALLOWED = "ERROR SPEED NOT ALLOWED";
    public static final String NO_SPEEDPASS = "ERROR NO SPEEDPASS AVAILABLE";
    public static final String NOT_VALID_TODAY = "ERROR TICKET NOT VALID TODAY";
    public static final String ALREADY_INSIDE = "ERROR USER ALREADY INSIDE";
    public static final String CAPACITY_EXCEEDED = "ERROR CAPACITY EXCEEDED";
    public static final String NOT_INSIDE = "ERROR NOT INSIDE";

    private static AttractionManager instance;

    private final AttractionRepository repository;
    private final TicketManager tickets;
    private final PassManager passes;
    private final SystemDate systemDate;
    private final Map<String, Attraction<?>> attractionsById = new LinkedHashMap<>();

    /**
     * Creates the unique AttractionManager with its injected dependencies, replacing any previous instance.
     * Called once at application startup.
     */
    public static AttractionManager initialize(AttractionRepository repository, TicketManager tickets,
                                               PassManager passes, SystemDate systemDate) throws IOException {
        instance = new AttractionManager(repository, tickets, passes, systemDate);
        return instance;
    }

    /**
     * Returns the unique AttractionManager; AttractionManager.initialize must have been called first.
     */
    public static AttractionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AttractionManager not initialized");
        }
        return instance;
    }

    private AttractionManager(AttractionRepository repository, TicketManager tickets, PassManager passes,
                              SystemDate systemDate)
            throws IOException {
        this.repository = repository;
        this.tickets = tickets;
        this.passes = passes;
        this.systemDate = systemDate;
        for (Attraction<?> attraction : repository.load()) {
            attractionsById.put(attraction.getId(), attraction);
        }
    }

    /**
     * Creates a GENERAL attraction.
     */
    public String create(String id, String name, String capacityText) {
        return create(id, name, capacityText, AttractionType.GENERAL.name());
    }

    public String create(String id, String name, String capacityText, String typeText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return UserManager.EMPTY_FIELD;
        }
        Integer capacity = Attraction.parseCapacity(capacityText);
        if (capacity == null) {
            return INVALID_CAPACITY;
        }
        AttractionType type = AttractionType.parse(typeText);
        if (type == null) {
            return INVALID_TYPE;
        }
        if (attractionsById.containsKey(id)) {
            return DUPLICATE_ATTRACTION;
        }
        attractionsById.put(id, type.create(id, name, capacity));
        return persist(ATTRACTION_CREATED);
    }

    public String delete(String id) {
        Attraction<?> attraction = attractionsById.get(id);
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
        return enter(attractionId, ticketId, false);
    }

    /**
     * Entry with a ticket; with {@code speed} the visitors use one SpeedPass each.
     */
    public String enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = attractionsById.get(attractionId);
        if (attraction == null) {
            return ATTRACTION_NOT_FOUND;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return TicketManager.TICKET_NOT_FOUND;
        }
        return enter(attraction, ticket, speed);
    }

    /**
     * Typed entry: the ticket is converted to the capability bound of the attraction before any
     * other rule is evaluated, so an attraction only ever admits tickets of its type parameter.
     * Every rule is checked before anybody enters or any pass is consumed.
     */
    private <T extends Admission> String enter(Attraction<T> attraction, Admission ticket, boolean speed) {
        if (!attraction.accepts(ticket)) {
            return TYPE_NOT_ACCEPTED;
        }
        if (speed && !attraction.allowsSpeed()) {
            return SPEED_NOT_ALLOWED;
        }
        T admission = attraction.cast(ticket);
        if (!admission.getDate().equals(systemDate.get())) {
            return NOT_VALID_TODAY;
        }
        List<User> visitors = admission.getVisitors();
        if (visitors.stream().anyMatch(this::isInsideAnyAttraction)) {
            return ALREADY_INSIDE;
        }
        if (!attraction.hasRoomFor(admission)) {
            return CAPACITY_EXCEEDED;
        }
        EntryCheck check = speed ? speedPassesFor(admission) : attraction.checkPasses(admission);
        if (check.isRejected()) {
            return check.getRejection();
        }
        attraction.enter(admission);
        List<PersonalPass> used = check.getPasses();
        if (!used.isEmpty()) {
            String stored = passes.consumeAll(used);
            if (UserManager.STORAGE_FAILURE.equals(stored)) {
                return stored;
            }
        }
        return "OK ENTERED " + visitors.size() + " USERS" + describe(used);
    }

    /**
     * One unused SpeedPass for every individual ticket of the admission, or a rejection.
     */
    private static EntryCheck speedPassesFor(Admission admission) {
        List<PersonalPass> selected = new ArrayList<>();
        for (IndividualTicket ticket : admission.getIndividualTickets()) {
            SpeedPass free = ticket.getSpeedPasses().stream().filter(p -> !p.isConsumed()).findFirst().orElse(null);
            if (free == null) {
                return EntryCheck.rejected(NO_SPEEDPASS);
            }
            selected.add(free);
        }
        return EntryCheck.passes(selected);
    }

    private static String describe(List<PersonalPass> used) {
        if (used.isEmpty()) {
            return "";
        }
        if (used.size() > 1) {
            return " (" + used.size() + " SPEEDPASSES CONSUMED)";
        }
        PersonalPass pass = used.get(0);
        String label = SpeedPass.KIND.equals(pass.getKind()) ? "SPEEDPASS" : "SPECIAL PASS";
        return " (" + label + " " + pass.getId() + " CONSUMED)";
    }

    public String exit(String attractionId, String ticketId) {
        Attraction<?> attraction = attractionsById.get(attractionId);
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
        Attraction<?> attraction = attractionsById.get(attractionId);
        if (attraction == null) {
            return ATTRACTION_NOT_FOUND;
        }
        List<String> lines = new ArrayList<>();
        lines.add("ATTRACTION " + attraction.getId() + " - " + attraction.getName());
        lines.add("Type: " + attraction.getType().name());
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

    public Attraction<?> find(String id) {
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
