package es.upm.amusementpark.command;

import java.util.List;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.ParkController;

/**
 * ATTRACTION commands.
 */
class AttractionCommands {

    private final ParkController park;

    AttractionCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] parts) {
        if (parts.length != 4 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use ATTRACTION CREATE;Identifier;Name;Capacity";
        }
        int capacity = parseCapacity(parts[3]);
        if (capacity < 1) {
            return "ERROR: Capacity must be a positive whole number";
        }
        if (park.getAttractions().exists(parts[1])) {
            return "ERROR: Attraction already exists";
        }
        park.getAttractions().add(new Attraction(parts[1], parts[2], capacity));
        return "ATTRACTION CREATED";
    }

    String list(String[] parts) {
        if (parts.length != 2 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use ATTRACTION LIST;AttractionIdentifier";
        }
        Attraction attraction = park.getAttractions().findById(parts[1]);
        if (attraction == null) {
            return "ERROR: Attraction not found";
        }
        if (attraction.occupancy() == 0) {
            return "No users in " + attraction.getId();
        }
        StringBuilder out = new StringBuilder("Users in " + attraction.getId() + ":");
        for (User user : attraction.getOccupants()) {
            out.append('\n').append(user.getEmail());
        }
        return out.toString();
    }

    String enter(String[] parts) {
        if (parts.length != 3 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier";
        }
        Attraction attraction = park.getAttractions().findById(parts[1]);
        if (attraction == null) {
            return "ERROR: Attraction not found";
        }
        Ticket ticket = park.getTickets().findById(parts[2]);
        if (ticket == null) {
            return "ERROR: Ticket not found";
        }
        if (ticket instanceof GroupTicket) {
            return "ERROR: Access with group tickets is not supported yet";
        }
        if (!ticket.isValidOn(park.getCurrentDate())) {
            return "ERROR: Ticket is not valid for the current date";
        }
        List<User> holders = ticket.holders();
        for (User user : holders) {
            if (attraction.isInside(user)) {
                return "ERROR: " + user.getEmail() + " is already inside " + attraction.getId();
            }
        }
        if (!attraction.hasRoomFor(holders.size())) {
            return "ERROR: Not enough capacity in " + attraction.getId();
        }
        StringBuilder out = new StringBuilder("ENTERED " + attraction.getId() + ":");
        for (User user : holders) {
            attraction.enter(user);
            out.append(' ').append(user.getEmail());
        }
        return out.toString();
    }

    /** Positive whole number written with digits only; -1 when invalid. */
    private static int parseCapacity(String text) {
        if (text.length() > 9) {
            return -1;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) < '0' || text.charAt(i) > '9') {
                return -1;
            }
        }
        return Integer.parseInt(text);
    }
}
