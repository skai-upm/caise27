package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.model.EntryResult;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;

/**
 * ATTRACTION ENTER;&lt;attraction&gt;;&lt;ticket&gt;[;SPEED].
 */
public class AttractionEnterCommand extends ControllerCommand<ParkController> {

    private static final String SPEED = "SPEED";

    public AttractionEnterCommand(ParkController controller) {
        super("ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]", 2, 3, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        if (fields.length == 3 && !fields[2].equals(SPEED)) {
            throw new InvalidValueException("Invalid entry option, expected SPEED: " + fields[2]);
        }
        EntryResult result = controller.enterAttraction(fields[0], fields[1], fields.length == 3);
        StringBuilder text = new StringBuilder("ATTRACTION ENTERED: " + result.users().size() + " user(s) in "
                + fields[0]);
        for (Pass pass : result.consumedPasses()) {
            text.append(System.lineSeparator())
                    .append(pass instanceof SpecialPass ? "SPECIALPASS CONSUMED: " : "SPEEDPASS CONSUMED: ")
                    .append(pass.getIdentifier());
        }
        return text.toString();
    }
}
