package es.upm.parque.command;

import java.util.List;

import es.upm.parque.model.Attraction;
import es.upm.parque.model.GroupTicket;
import es.upm.parque.model.Ticket;
import es.upm.parque.model.User;
import es.upm.parque.park.ParkController;

/**
 * Executes the ATTRACTION commands against the central park management.
 */
class AttractionCommands {

    static final String ATTRACTION_CREATE = "ATTRACTION CREATE";
    static final String ATTRACTION_LIST = "ATTRACTION LIST";
    static final String ATTRACTION_ENTER = "ATTRACTION ENTER";

    private final ParkController park;

    AttractionCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] e) {
        if (e.length != 4 || e[1].isEmpty() || e[2].isEmpty()) {
            return error("ATTRACTION CREATE expects: ATTRACTION CREATE;<id>;<name>;<capacity>");
        }
        if (park.getAttractions().exists(e[1])) {
            return error("an attraction with identifier " + e[1] + " already exists");
        }
        int capacity = parseCapacity(e[3]);
        if (capacity <= 0) {
            return error("invalid capacity '" + e[3] + "' (expected a positive whole number)");
        }
        park.getAttractions().add(new Attraction(e[1], e[2], capacity));
        return "OK: attraction " + e[1] + " (" + e[2] + ") created with capacity " + capacity;
    }

    String list(String[] e) {
        if (e.length != 2) {
            return error("ATTRACTION LIST expects: ATTRACTION LIST;<id>");
        }
        Attraction attraction = park.getAttractions().findById(e[1]);
        if (attraction == null) {
            return error("no attraction with identifier " + e[1]);
        }
        String header = "OK: attraction " + attraction.getId() + " (" + attraction.getName() + ") has ";
        if (attraction.getOccupants().isEmpty()) {
            return header + "no users inside";
        }
        StringBuilder text = new StringBuilder(header)
                .append(attraction.getOccupants().size()).append(" user(s) inside:");
        for (User user : attraction.getOccupants()) {
            text.append(System.lineSeparator()).append("  ").append(user.getEmail());
        }
        return text.toString();
    }

    String enter(String[] e) {
        if (e.length != 3) {
            return error("ATTRACTION ENTER expects: ATTRACTION ENTER;<attraction>;<ticket>");
        }
        Attraction attraction = park.getAttractions().findById(e[1]);
        if (attraction == null) {
            return error("no attraction with identifier " + e[1]);
        }
        Ticket ticket = park.getTickets().findById(e[2]);
        if (ticket == null) {
            return error("no ticket with identifier " + e[2]);
        }
        if (ticket instanceof GroupTicket) {
            return error("access with group tickets is not available yet");
        }
        if (!ticket.getDate().equals(park.getCurrentDate())) {
            return error("ticket " + e[2] + " is valid on " + ticket.getDate() + ", not on " + park.getCurrentDate());
        }
        List<User> entrants = ticket.getHolders().stream().filter(u -> !attraction.isInside(u)).toList();
        if (entrants.isEmpty()) {
            return error("the users of ticket " + e[2] + " are already inside attraction " + e[1]);
        }
        int free = attraction.getCapacity() - attraction.getOccupants().size();
        if (entrants.size() > free) {
            return error("attraction " + e[1] + " has room for " + free + " user(s) but " + entrants.size()
                    + " need to enter; nobody entered");
        }
        entrants.forEach(attraction::addOccupant);
        return "OK: " + entrants.size() + " user(s) entered attraction " + attraction.getId() + " ("
                + attraction.getName() + "): "
                + String.join(", ", entrants.stream().map(User::getEmail).toList());
    }

    /** Parses a whole number made only of digits; returns -1 when it is not one or does not fit an int. */
    static int parseCapacity(String text) {
        if (text.isEmpty() || text.length() > 9) {
            return -1;
        }
        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            value = value * 10 + (c - '0');
        }
        return value;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
