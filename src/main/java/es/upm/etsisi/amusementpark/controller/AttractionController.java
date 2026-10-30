package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.Attraction;
import es.upm.etsisi.amusementpark.model.Ticket;
import es.upm.etsisi.amusementpark.model.User;

import java.util.List;

import static es.upm.etsisi.amusementpark.controller.UserController.requireValid;

/**
 * Attraction operations: creation, access with any ticket type, exit and queries.
 */
public class AttractionController {

    private final ParkController park;

    public AttractionController(ParkController park) {
        this.park = park;
    }

    public void create(String id, String name, String capacityText) {
        requireValid(!id.isBlank(), "Attraction identifier cannot be empty");
        requireValid(park.attractions().findById(id).isEmpty(), "Attraction already exists: " + id);
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid capacity: " + capacityText);
        }
        requireValid(capacity > 0, "Capacity must be positive: " + capacityText);
        park.attractions().add(new Attraction(id, name, capacity));
    }

    /**
     * Lets every holder of the ticket in, or nobody.
     *
     * @return number of users who entered
     */
    public int enter(String attractionId, String ticketId) {
        Attraction attraction = find(attractionId);
        Ticket ticket = park.tickets().findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        requireValid(ticket.isValidOn(park.today()),
                "Ticket " + ticketId + " is valid on " + ticket.getDate() + ", not on " + park.today());
        List<User> holders = ticket.getHolders();
        attraction.enter(holders);
        return holders.size();
    }

    public void exit(String attractionId, String email) {
        Attraction attraction = find(attractionId);
        User user = attraction.getOccupants().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not inside " + attractionId + ": " + email));
        attraction.exit(user);
    }

    public Attraction find(String attractionId) {
        return park.attractions().findById(attractionId)
                .orElseThrow(() -> new IllegalArgumentException("Attraction not found: " + attractionId));
    }
}
