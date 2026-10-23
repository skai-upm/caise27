package es.upm.park.service;

import es.upm.park.model.Attraction;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.validation.UserValidator;

import java.util.List;

/**
 * Attraction operations. Entry works on the common {@link Ticket}
 * abstraction, so individual and group tickets share one access path.
 */
public class AttractionService {

    private final Park park;

    public AttractionService(Park park) {
        this.park = park;
    }

    public Attraction create(String id, String name, int capacity) throws OperationException {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new OperationException("The attraction identifier and name must not be empty");
        }
        if (park.findAttraction(id) != null) {
            throw new OperationException("An attraction with identifier " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        park.addAttraction(attraction);
        park.save();
        return attraction;
    }

    /**
     * Admits every user of the ticket, or nobody.
     *
     * @return the users who entered
     */
    public List<User> enter(String attractionId, String ticketId) throws OperationException {
        Attraction attraction = existingAttraction(attractionId);
        Ticket ticket = park.findTicket(ticketId);
        if (ticket == null) {
            throw new OperationException("No ticket with identifier " + ticketId);
        }
        if (!ticket.isValidOn(park.getCurrentDate())) {
            throw new OperationException("Ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on the current date " + park.getCurrentDate());
        }
        List<User> holders = ticket.getHolders();
        for (User user : holders) {
            Attraction current = attractionOf(user);
            if (current != null) {
                throw new OperationException("The user " + user.getEmail() + " is already inside attraction "
                        + current.getId());
            }
        }
        if (!attraction.enter(holders)) {
            throw new OperationException("Not enough places in " + attractionId + ": " + holders.size()
                    + " needed, " + attraction.getAvailablePlaces() + " available");
        }
        return holders;
    }

    public void exit(String attractionId, String email) throws OperationException {
        Attraction attraction = existingAttraction(attractionId);
        for (User user : attraction.getOccupants()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                attraction.exit(user);
                return;
            }
        }
        throw new OperationException("The user " + email + " is not inside attraction " + attractionId);
    }

    public Attraction existingAttraction(String id) throws OperationException {
        Attraction attraction = park.findAttraction(id);
        if (attraction == null) {
            throw new OperationException("No attraction with identifier " + id);
        }
        return attraction;
    }

    private Attraction attractionOf(User user) {
        for (Attraction attraction : park.attractions()) {
            if (attraction.isInside(user)) {
                return attraction;
            }
        }
        return null;
    }
}
