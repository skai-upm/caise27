package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.PrintStream;

/**
 * Base for commands with a fixed number of non-empty fields that operate on the
 * park controller. Rejected operations are reported as {@code ERROR: ...} lines.
 */
public abstract class ControllerCommand implements Command {

    private final String usage;
    private final int fieldCount;

    protected ControllerCommand(String usage, int fieldCount) {
        this.usage = usage;
        this.fieldCount = fieldCount;
    }

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        if (arguments.length != fieldCount) {
            out.println("ERROR: Usage: " + usage);
            return false;
        }
        String[] fields = new String[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            fields[i] = arguments[i].trim();
            if (fields[i].isEmpty()) {
                out.println("ERROR: Empty field in: " + usage);
                return false;
            }
        }
        try {
            out.println(run(ParkController.getInstance(), fields));
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return false;
    }

    /**
     * Performs the operation and returns the success message.
     */
    protected abstract String run(ParkController controller, String[] fields);
}
