package es.upm.amusementpark;

import java.util.List;

/**
 * Attraction management operations on the park's attractions.
 */
public class AttractionService {

    private final ParkController park = ParkController.getInstance();

    public Attraction createAttraction(String identifier, String name, int capacity)
            throws AttractionOperationException {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new AttractionOperationException("The attraction identifier and name cannot be empty.");
        }
        if (capacity < 1) {
            throw new AttractionOperationException("The capacity must be a positive whole number.");
        }
        if (park.getAttractions().findById(identifier).isPresent()) {
            throw new AttractionOperationException("An attraction with identifier " + identifier + " already exists.");
        }
        Attraction attraction = new Attraction(identifier, name, capacity);
        park.getAttractions().save(attraction);
        return attraction;
    }

    /**
     * Lets the users admitted by the ticket into the attraction. The ticket
     * must be for the current system date and there must be room for all of
     * them.
     */
    public List<User> enter(String attractionId, String ticketId) throws AttractionOperationException {
        Attraction attraction = findAttraction(attractionId);
        Ticket ticket = park.getTickets().findById(ticketId).orElseThrow(
                () -> new AttractionOperationException("No ticket with identifier " + ticketId + " exists."));
        if (ticket instanceof GroupTicket) {
            throw new AttractionOperationException("Access with group tickets is not available yet.");
        }
        if (!ticket.getDate().equals(park.getCurrentDate())) {
            throw new AttractionOperationException("Ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on the current date " + park.getCurrentDate() + ".");
        }
        List<User> holders = ticket.getHolders();
        for (User user : holders) {
            if (attraction.isInside(user)) {
                throw new AttractionOperationException("User " + user.getEmail() + " is already inside attraction "
                        + attractionId + ".");
            }
        }
        if (holders.size() > attraction.getAvailablePlaces()) {
            throw new AttractionOperationException("Attraction " + attractionId + " does not have enough capacity ("
                    + attraction.getAvailablePlaces() + " places available).");
        }
        attraction.enter(holders);
        return holders;
    }

    public Attraction findAttraction(String identifier) throws AttractionOperationException {
        return park.getAttractions().findById(identifier).orElseThrow(
                () -> new AttractionOperationException("No attraction with identifier " + identifier + " exists."));
    }
}
