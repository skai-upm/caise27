package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.Attraction;
import es.upm.etsisi.amusementpark.model.User;

/**
 * ATTRACTION LIST;AttractionIdentifier: read-only listing of the users inside.
 */
public class AttractionListCommand extends ControllerCommand {

    public AttractionListCommand() {
        super("ATTRACTION LIST;<AttractionIdentifier>", 1);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        Attraction attraction = controller.findAttraction(fields[0])
                .orElseThrow(() -> new IllegalArgumentException("Attraction not found: " + fields[0]));
        if (attraction.getOccupants().isEmpty()) {
            return "NO USERS IN ATTRACTION " + attraction.getIdentifier();
        }
        StringBuilder emails = new StringBuilder();
        for (User user : attraction.getOccupants()) {
            if (emails.length() > 0) {
                emails.append(System.lineSeparator());
            }
            emails.append(user.getEmail());
        }
        return emails.toString();
    }
}
