package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.AccessCredential;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.AttractionRepository;
import es.upm.parque.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the park attractions (Singleton) and applies the entry rules: ticket for the system date,
 * admission by attraction type, registered holders, and SpeedPass/Special Pass consumption.
 */
public final class AttractionController {

    public static final String SPEED_OPTION = "SPEED";

    private static final AttractionController INSTANCE = new AttractionController();

    private final List<Attraction<?>> attractions = new ArrayList<>();
    private AttractionRepository repository;
    private TicketController tickets;
    private PassController passes;
    private UserController users;
    private ParkController park;

    /**
     * Outcome of a successful entry: how many users entered and which passes were consumed.
     */
    public record EntryResult(int entered, List<Pass> consumedPasses) {

        public EntryResult {
            consumedPasses = List.copyOf(consumedPasses);
        }
    }

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(AttractionRepository repository, TicketController tickets, PassController passes,
                          UserController users, ParkController park) {
        this.repository = repository;
        this.tickets = tickets;
        this.passes = passes;
        this.users = users;
        this.park = park;
    }

    public void load() {
        attractions.clear();
        attractions.addAll(repository.load());
    }

    public List<Attraction<?>> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction<?> get(String id) {
        return attractions.stream().filter(attraction -> attraction.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ParkException("attraction " + id + " does not exist"));
    }

    /**
     * Creates an attraction; a null type means GENERAL.
     */
    public Attraction<?> create(String id, String name, String capacityText, String typeText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ParkException("attraction identifier and name must not be empty");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new ParkException("capacity must be a whole number: " + capacityText);
        }
        if (capacity <= 0) {
            throw new ParkException("capacity must be positive");
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromName(typeText);
        if (type == null) {
            throw new ParkException("unknown attraction type " + typeText + "; use GENERAL, INDIVIDUAL, GROUP or SPECIAL");
        }
        if (attractions.stream().anyMatch(attraction -> attraction.getId().equals(id))) {
            throw new ParkException("attraction " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        save();
        return attraction;
    }

    public void delete(String id) {
        Attraction<?> attraction = get(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new ParkException("attraction " + id + " is not empty");
        }
        attractions.remove(attraction);
        save();
    }

    /**
     * Admits the users of a ticket. {@code option} is null for a normal entry or {@code SPEED}.
     * Nothing changes if any check fails.
     */
    public EntryResult enter(String attractionId, String ticketId, String option) {
        boolean speed = false;
        if (option != null) {
            if (!SPEED_OPTION.equals(option.trim())) {
                throw new ParkException("unknown entry option " + option + "; only " + SPEED_OPTION + " is allowed");
            }
            speed = true;
        }
        return enter(get(attractionId), tickets.get(ticketId), speed);
    }

    public int exit(String attractionId, String ticketId) {
        return get(attractionId).exit(tickets.get(ticketId));
    }

    private <T extends AccessCredential> EntryResult enter(Attraction<T> attraction, Ticket ticket, boolean speed) {
        if (!ticket.getDate().equals(park.getCurrentDate())) {
            throw new ParkException("ticket " + ticket.getId() + " is for " + ticket.getDate()
                    + ", not for the current date " + park.getCurrentDate());
        }
        if (speed && !attraction.allowsSpeed()) {
            throw new ParkException(attraction.getType() + " attraction " + attraction.getId() + " does not accept " + SPEED_OPTION);
        }
        T credential = attraction.admit(ticket);
        for (User user : credential.getUsers()) {
            if (!users.isRegistered(user)) {
                throw new ParkException("the holder " + user.getDni() + " of ticket " + ticket.getId() + " is no longer registered");
            }
        }
        List<Pass> toConsume = new ArrayList<>();
        for (IndividualTicket individual : credential.getIndividualTickets()) {
            if (speed) {
                SpeedPass speedPass = passes.findUnusedSpeedPass(individual);
                if (speedPass == null) {
                    throw new ParkException("ticket " + individual.getId() + " has no unused SpeedPass");
                }
                toConsume.add(speedPass);
            }
            if (attraction.requiresSpecialPass()) {
                SpecialPass specialPass = passes.findSpecialPass(individual);
                if (specialPass == null || specialPass.isConsumed()) {
                    throw new ParkException("ticket " + individual.getId() + " has no unused Special Pass");
                }
                toConsume.add(specialPass);
            }
        }
        int entered = attraction.enter(credential);
        if (!toConsume.isEmpty()) {
            passes.consume(toConsume);
        }
        return new EntryResult(entered, toConsume);
    }

    private void save() {
        repository.saveAll(attractions);
    }
}
