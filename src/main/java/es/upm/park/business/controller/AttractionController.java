package es.upm.park.business.controller;

import es.upm.park.business.exception.AccessDeniedException;
import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.access.GeneralAccess;
import es.upm.park.business.model.access.GroupAccess;
import es.upm.park.business.model.attraction.Attraction;
import es.upm.park.business.model.attraction.AttractionType;
import es.upm.park.business.model.attraction.EntryResult;
import es.upm.park.business.validation.UserValidator;

/**
 * Attraction operations. Entry works on the ticket access capabilities, so
 * individual and group tickets share one access path while each attraction
 * type restricts the capability it accepts. Passes are looked up through
 * the injected {@link PassController}. Single instance.
 */
public final class AttractionController {

    private Park park;
    private PassController passes;

    private AttractionController() {
    }

    private static final class Holder {
        private static final AttractionController INSTANCE = new AttractionController();
    }

    public static AttractionController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    private Park park() {
        if (park == null) {
            throw new IllegalStateException("AttractionController has not been configured");
        }
        return park;
    }

    public Attraction<?> create(String id, String name, int capacity, AttractionType type) throws ParkException {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ValidationException("The attraction identifier and name must not be empty");
        }
        if (park().findAttraction(id) != null) {
            throw new ConflictException("An attraction with identifier " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        park().addAttraction(attraction);
        park().save();
        return attraction;
    }

    /**
     * Admits every user of the ticket and consumes the passes the entry
     * needs, or changes nothing.
     */
    public EntryResult enter(String attractionId, String ticketId, boolean speed) throws ParkException {
        Attraction<?> attraction = existingAttraction(attractionId);
        Ticket ticket = park().findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("No ticket with identifier " + ticketId);
        }
        if (!ticket.isValidOn(park().getCurrentDate())) {
            throw new BusinessRuleException("Ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on the current date " + park().getCurrentDate());
        }
        if (!attraction.accepts(ticket)) {
            String kind = ticket instanceof GroupAccess ? "group" : "individual";
            throw new AccessDeniedException("Attraction " + attractionId + " (" + attraction.getType()
                    + ") does not accept " + kind + " tickets");
        }
        for (User user : ticket.getHolders()) {
            Attraction<?> current = attractionOf(user);
            if (current != null) {
                throw new BusinessRuleException("The user " + user.getEmail() + " is already inside attraction "
                        + current.getId());
            }
        }
        EntryResult result = admit(attraction, ticket, speed);
        if (!result.consumedPasses().isEmpty()) {
            park().save();
        }
        return result;
    }

    /**
     * Narrows the ticket to the capability the attraction is declared for.
     */
    private <T extends GeneralAccess> EntryResult admit(Attraction<T> attraction, GeneralAccess ticket, boolean speed)
            throws AccessDeniedException {
        return attraction.enter(attraction.getAccessType().cast(ticket), speed, passes);
    }

    public void exit(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = existingAttraction(attractionId);
        for (User user : attraction.getOccupants()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                attraction.exit(user);
                return;
            }
        }
        throw new NotFoundException("The user " + email + " is not inside attraction " + attractionId);
    }

    public Attraction<?> existingAttraction(String id) throws NotFoundException {
        Attraction<?> attraction = park().findAttraction(id);
        if (attraction == null) {
            throw new NotFoundException("No attraction with identifier " + id);
        }
        return attraction;
    }

    private Attraction<?> attractionOf(User user) {
        for (Attraction<?> attraction : park().attractions()) {
            if (attraction.isInside(user)) {
                return attraction;
            }
        }
        return null;
    }
}
