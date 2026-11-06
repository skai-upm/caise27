package es.upm.park.business.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.park.business.model.Admission;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.IndividualAccess;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.PassLookup;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.repository.AttractionRepository;

/**
 * Operations on attractions.
 */
public class AttractionService {

    private static final int MAX_CAPACITY_DIGITS = 9;

    private static final AttractionService INSTANCE = new AttractionService();

    private AttractionRepository attractions;

    private AttractionService() {
    }

    /** The single AttractionService instance of the execution. */
    public static AttractionService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies chosen during application initialization.
     *
     * @return this controller
     */
    public AttractionService configure(AttractionRepository attractions) {
        this.attractions = attractions;
        return this;
    }

    public Attraction<?> create(String id, String name, String capacity) {
        return create(id, name, capacity, AttractionType.GENERAL.name());
    }

    /** Creates an attraction of the given type ({@code GENERAL}, {@code INDIVIDUAL}, ...). */
    public Attraction<?> create(String id, String name, String capacity, String typeName) {
        if (id == null || id.isBlank()) {
            throw new AttractionException("attraction identifier is required");
        }
        if (attractions.findById(id).isPresent()) {
            throw new AttractionException("attraction " + id + " already exists");
        }
        if (name == null || name.isBlank()) {
            throw new AttractionException("attraction name is required");
        }
        int places = parseCapacity(capacity);
        AttractionType type = AttractionType.fromName(typeName);
        if (type == null) {
            throw new AttractionException("invalid attraction type " + typeName);
        }
        Attraction<?> attraction = type.create(id, name, places);
        attractions.add(attraction);
        return attraction;
    }

    public Attraction<?> find(String id) {
        return attractions.findById(id).orElseThrow(() -> new AttractionException("no attraction " + id));
    }

    /** Emails of the users currently inside; read-only. */
    public List<String> occupantEmails(String id) {
        return find(id).getOccupants().stream().map(User::getEmail).toList();
    }

    /**
     * Admits the users of the ticket into the attraction without SpeedPasses.
     *
     * @return the users who entered
     */
    public List<User> enter(String attractionId, Ticket ticket, LocalDate today, PassLookup passes) {
        return enter(attractionId, ticket, today, passes, false).users();
    }

    /**
     * Admits the users of the ticket into the attraction, optionally using
     * SpeedPasses, or nobody: every condition (ticket type, validity, the
     * attraction's own requirements, SpeedPass availability, presence and
     * capacity) is checked before entering and consuming passes together.
     *
     * @return who entered and which passes were consumed
     */
    public AccessResult enter(String attractionId, Ticket ticket, LocalDate today, PassLookup passes,
            boolean speed) {
        return admit(find(attractionId), ticket, today, passes, speed);
    }

    private static <T extends Admission> AccessResult admit(Attraction<T> attraction, Ticket ticket,
            LocalDate today, PassLookup passes, boolean speed) {
        T admission = attraction.admissible(ticket).orElseThrow(() -> new AttractionException("attraction "
                + attraction.getId() + " (" + attraction.getType() + ") does not accept ticket " + ticket.getId()));
        if (!admission.isValidOn(today)) {
            throw new AttractionException("ticket " + ticket.getId() + " is not valid on " + today);
        }
        attraction.missingRequirement(admission, passes).ifPresent(reason -> {
            throw new AttractionException(reason);
        });
        List<Pass> toConsume = new ArrayList<>(attraction.automaticPasses(admission, passes));
        if (speed) {
            toConsume.addAll(speedPasses(attraction, admission, passes));
        }
        List<User> holders = attraction.holdersOf(admission);
        for (User user : holders) {
            if (attraction.contains(user)) {
                throw new AttractionException("user " + user.getEmail() + " is already inside attraction "
                        + attraction.getId());
            }
        }
        if (attraction.getOccupants().size() + holders.size() > attraction.getCapacity()) {
            throw new AttractionException("attraction " + attraction.getId() + " does not have capacity for "
                    + holders.size() + " users");
        }
        holders.forEach(attraction::enter);
        toConsume.forEach(Pass::consume);
        return new AccessResult(holders, toConsume);
    }

    private static <T extends Admission> List<Pass> speedPasses(Attraction<T> attraction, T admission,
            PassLookup passes) {
        if (!attraction.acceptsSpeedPasses()) {
            throw new AttractionException("attraction " + attraction.getId() + " (" + attraction.getType()
                    + ") does not accept SpeedPasses");
        }
        List<Pass> selected = new ArrayList<>();
        for (IndividualAccess ticket : attraction.speedPassTickets(admission)) {
            selected.add(passes.passesOf(ticket).stream().filter(p -> p instanceof SpeedPass && !p.isConsumed())
                    .findFirst().orElseThrow(() -> new AttractionException("ticket " + ticket.getId()
                            + " has no unconsumed SpeedPass")));
        }
        return selected;
    }

    /**
     * The user with that email leaves the attraction, releasing one place.
     *
     * @return the user who left
     */
    public User exit(String attractionId, String email) {
        Attraction<?> attraction = find(attractionId);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new AttractionException("user " + email + " is not inside attraction "
                        + attractionId));
        attraction.leave(user);
        return user;
    }

    /** Status text built from the attraction's own state; read-only. */
    public String status(String attractionId) {
        Attraction<?> a = find(attractionId);
        int inside = a.getOccupants().size();
        List<String> emails = a.getOccupants().stream().map(User::getEmail).toList();
        return String.join(System.lineSeparator(),
                "Attraction " + a.getId() + " - " + a.getName(),
                "Capacity: " + a.getCapacity(),
                "Inside: " + inside,
                "Available: " + (a.getCapacity() - inside),
                "Users: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }

    private static int parseCapacity(String text) {
        boolean digits = text != null && !text.isEmpty() && text.length() <= MAX_CAPACITY_DIGITS
                && text.chars().allMatch(c -> c >= '0' && c <= '9');
        int value = digits ? Integer.parseInt(text) : 0;
        if (value <= 0) {
            throw new AttractionException("invalid capacity " + text);
        }
        return value;
    }
}
