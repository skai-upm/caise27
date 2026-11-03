package es.upm.etsisi.park;

import java.util.List;

/**
 * Attraction operations. Entering and leaving only change runtime occupancy.
 */
public class AttractionService {

    private final Park park;

    public AttractionService(Park park) {
        this.park = park;
    }

    public void create(String id, String name, String capacityText) throws ValidationException {
        GroupService.requireText(id, "attraction identifier");
        GroupService.requireText(name, "attraction name");
        if (park.findAttraction(id) != null) {
            throw new ValidationException("attraction " + id + " already exists");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            throw new ValidationException("invalid capacity " + capacityText + ": expected a positive integer");
        }
        if (capacity <= 0) {
            throw new ValidationException("invalid capacity " + capacityText + ": expected a positive integer");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        park.addAttraction(attraction);
        park.commit(() -> park.removeAttraction(attraction));
    }

    /**
     * Lets every holder of the ticket enter, or none of them.
     *
     * @return the number of users who entered
     */
    public int enter(String attractionId, String ticketId) throws ValidationException {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = park.findTicket(ticketId);
        if (ticket == null) {
            throw new ValidationException("no ticket with identifier " + ticketId);
        }
        if (!ticket.isValidOn(park.getCurrentDate())) {
            throw new ValidationException("ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on the current system date " + park.getCurrentDate());
        }
        List<User> holders = ticket.getHolders();
        attraction.enter(holders);
        return holders.size();
    }

    public void exit(String attractionId, String email) throws ValidationException {
        Attraction attraction = requireAttraction(attractionId);
        User user = attraction.findOccupant(email);
        if (user == null) {
            throw new ValidationException("user " + email + " is not inside attraction " + attractionId);
        }
        attraction.exit(user);
    }

    public Attraction requireAttraction(String id) throws ValidationException {
        Attraction attraction = park.findAttraction(id);
        if (attraction == null) {
            throw new ValidationException("no attraction with identifier " + id);
        }
        return attraction;
    }
}
