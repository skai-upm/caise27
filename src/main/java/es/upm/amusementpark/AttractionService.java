package es.upm.amusementpark;

import java.util.List;

/**
 * Attraction operations: creation, access with any ticket type, exit and queries.
 */
public class AttractionService {

    private final Park park = Park.getInstance();

    public Attraction create(String id, String name, String capacityText) {
        int capacity = ParkFormat.parseCapacity(capacityText);
        if (park.getAttractions().findById(id) != null) {
            throw new IllegalArgumentException("an attraction with identifier " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        park.getAttractions().add(attraction);
        return attraction;
    }

    /**
     * Lets in every holder of the ticket at once. Works the same for individual and group tickets.
     *
     * @return the users who entered
     */
    public List<User> enter(String attractionId, String ticketId) {
        Attraction attraction = existing(attractionId);
        Ticket ticket = park.getTickets().findById(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("no ticket with identifier " + ticketId);
        }
        if (!ticket.isValidOn(park.getDate())) {
            throw new IllegalArgumentException("ticket " + ticketId + " is valid on "
                    + ParkFormat.formatDate(ticket.getDate()) + ", not on the current date "
                    + ParkFormat.formatDate(park.getDate()));
        }
        List<User> holders = ticket.getHolders();
        for (User holder : holders) {
            if (!park.getUsers().contains(holder)) {
                throw new IllegalArgumentException("ticket holder " + holder.getDni()
                        + " is no longer a registered user");
            }
        }
        attraction.enter(holders);
        return holders;
    }

    public void exit(String attractionId, String email) {
        Attraction attraction = existing(attractionId);
        for (User occupant : attraction.getOccupants()) {
            if (occupant.getEmail().equals(email)) {
                attraction.exit(occupant);
                return;
            }
        }
        throw new IllegalArgumentException("no user with email " + email + " is inside attraction " + attractionId);
    }

    public Attraction existing(String attractionId) {
        Attraction attraction = park.getAttractions().findById(attractionId);
        if (attraction == null) {
            throw new IllegalArgumentException("no attraction with identifier " + attractionId);
        }
        return attraction;
    }
}
