package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.User;

/**
 * ATTRACTION STATUS;AttractionIdentifier: state of an attraction from its own data.
 */
public class AttractionStatusCommand extends ControllerCommand<ParkController> {

    public AttractionStatusCommand(ParkController controller) {
        super("ATTRACTION STATUS;<AttractionIdentifier>", 1, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        Attraction attraction = controller.findAttraction(fields[0])
                .orElseThrow(() -> new NotFoundException("Attraction not found: " + fields[0]));
        StringBuilder occupants = new StringBuilder();
        for (User user : attraction.getOccupants()) {
            if (occupants.length() > 0) {
                occupants.append(", ");
            }
            occupants.append(user.getEmail());
        }
        String nl = System.lineSeparator();
        return "ATTRACTION: " + attraction.getIdentifier() + nl
                + "NAME: " + attraction.getName() + nl
                + "CAPACITY: " + attraction.getCapacity() + nl
                + "USERS INSIDE: " + attraction.getOccupants().size() + nl
                + "AVAILABLE PLACES: " + attraction.getAvailablePlaces() + nl
                + "OCCUPANTS: " + (occupants.length() == 0 ? "(none)" : occupants);
    }
}
