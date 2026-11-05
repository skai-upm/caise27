package es.upm.parque.presentation;

import java.util.List;

import es.upm.parque.business.AttractionManager;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.User;

/**
 * Parses the ATTRACTION commands and formats their responses; the rules live in {@link AttractionManager}.
 */
class AttractionCommands {

    static final String ATTRACTION_CREATE = "ATTRACTION CREATE";
    static final String ATTRACTION_LIST = "ATTRACTION LIST";
    static final String ATTRACTION_ENTER = "ATTRACTION ENTER";
    static final String ATTRACTION_EXIT = "ATTRACTION EXIT";
    static final String ATTRACTION_STATUS = "ATTRACTION STATUS";

    private final AttractionManager attractions;

    AttractionCommands(AttractionManager attractions) {
        this.attractions = attractions;
    }

    String create(String[] e) throws InvalidCommandException, ParkException {
        if (e.length != 4 || e[1].isEmpty() || e[2].isEmpty()) {
            throw new InvalidCommandException("ATTRACTION CREATE expects: ATTRACTION CREATE;<id>;<name>;<capacity>");
        }
        Attraction a = attractions.create(e[1], e[2], e[3]);
        return "OK: attraction " + a.getId() + " (" + a.getName() + ") created with capacity " + a.getCapacity();
    }

    String list(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "ATTRACTION LIST expects: ATTRACTION LIST;<id>");
        Attraction a = attractions.find(e[1]);
        String header = "OK: attraction " + a.getId() + " (" + a.getName() + ") has ";
        if (a.getOccupants().isEmpty()) {
            return header + "no users inside";
        }
        return header + a.getOccupants().size() + " user(s) inside:" + occupantLines(a);
    }

    String enter(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 3, "ATTRACTION ENTER expects: ATTRACTION ENTER;<attraction>;<ticket>");
        List<User> entrants = attractions.enter(e[1], e[2]);
        Attraction a = attractions.find(e[1]);
        return "OK: " + entrants.size() + " user(s) entered attraction " + a.getId() + " (" + a.getName() + "): "
                + String.join(", ", entrants.stream().map(User::getEmail).toList());
    }

    String exit(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 3, "ATTRACTION EXIT expects: ATTRACTION EXIT;<attraction>;<email>");
        Attraction a = attractions.exit(e[1], e[2]);
        return "OK: " + e[2] + " left attraction " + a.getId() + " (" + a.getName() + ")";
    }

    String status(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "ATTRACTION STATUS expects: ATTRACTION STATUS;<id>");
        Attraction a = attractions.find(e[1]);
        int inside = a.getOccupants().size();
        return "OK: attraction " + a.getId() + " (" + a.getName() + ") - capacity " + a.getCapacity() + ", inside "
                + inside + ", available " + (a.getCapacity() - inside) + occupantLines(a);
    }

    private static String occupantLines(Attraction a) {
        StringBuilder text = new StringBuilder();
        for (User user : a.getOccupants()) {
            text.append(System.lineSeparator()).append("  ").append(user.getEmail());
        }
        return text.toString();
    }
}
