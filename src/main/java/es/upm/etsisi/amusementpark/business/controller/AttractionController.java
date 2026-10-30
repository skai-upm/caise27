package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.DuplicateException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.EntryResult;
import es.upm.etsisi.amusementpark.business.model.PassLookup;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.TicketAccess;
import es.upm.etsisi.amusementpark.business.model.User;

/**
 * Attraction management (Singleton): creation, typed access with passes, exit and queries.
 * Pass consumption is delegated to the injected {@link PassController}.
 */
public final class AttractionController {

    private static final AttractionController INSTANCE = new AttractionController();

    private ParkController park;
    private PassController passes;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    public Attraction<?> create(String id, String name, String capacityText) {
        return create(id, name, capacityText, AttractionType.GENERAL.name());
    }

    public Attraction<?> create(String id, String name, String capacityText, String typeText) {
        if (id.isBlank()) {
            throw new ValidationException("Attraction identifier cannot be empty");
        }
        if (park.attractions().findById(id).isPresent()) {
            throw new DuplicateException("Attraction already exists: " + id);
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new ValidationException("Capacity must be positive: " + capacityText);
        }
        AttractionType type = AttractionType.fromName(typeText.trim());
        if (type == null) {
            throw new ValidationException("Unknown attraction type: " + typeText);
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        park.attractions().add(attraction);
        return attraction;
    }

    /**
     * Lets every holder of the ticket in, consuming the passes the attraction requires, or changes nothing.
     */
    public EntryResult enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = find(attractionId);
        Ticket ticket = park.tickets().findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
        if (!ticket.isValidOn(park.today())) {
            throw new OperationNotAllowedException(
                    "Ticket " + ticketId + " is valid on " + ticket.getDate() + ", not on " + park.today());
        }
        EntryResult result = enter(attraction, ticket, speed, passes);
        if (!result.consumedPasses().isEmpty()) {
            passes.save();
        }
        return result;
    }

    public void exit(String attractionId, String email) {
        Attraction<?> attraction = find(attractionId);
        User user = attraction.getOccupants().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not inside " + attractionId + ": " + email));
        attraction.exit(user);
    }

    public Attraction<?> find(String attractionId) {
        return park.attractions().findById(attractionId)
                .orElseThrow(() -> new NotFoundException("Attraction not found: " + attractionId));
    }

    private static <T extends TicketAccess> EntryResult enter(Attraction<T> attraction, Ticket ticket,
                                                              boolean speed, PassLookup lookup) {
        return attraction.enter(attraction.asAccepted(ticket), speed, lookup);
    }
}
