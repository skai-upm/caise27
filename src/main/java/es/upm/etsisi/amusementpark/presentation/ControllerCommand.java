package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.exception.ParkException;

import java.io.PrintStream;

/**
 * Base for commands with a fixed number of non-empty fields that operate on an
 * injected controller. Rejected operations are reported as {@code ERROR: ...} lines.
 *
 * @param <C> type of the controller the command works with
 */
public abstract class ControllerCommand<C> implements Command {

    private final String usage;
    private final int minFields;
    private final int maxFields;
    private final C controller;

    protected ControllerCommand(String usage, int fieldCount, C controller) {
        this(usage, fieldCount, fieldCount, controller);
    }

    protected ControllerCommand(String usage, int minFields, int maxFields, C controller) {
        this.usage = usage;
        this.minFields = minFields;
        this.maxFields = maxFields;
        this.controller = controller;
    }

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        if (arguments.length < minFields || arguments.length > maxFields) {
            out.println("ERROR: Usage: " + usage);
            return false;
        }
        String[] fields = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            fields[i] = arguments[i].trim();
            if (fields[i].isEmpty()) {
                out.println("ERROR: Empty field in: " + usage);
                return false;
            }
        }
        try {
            out.println(run(controller, fields));
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return false;
    }

    /**
     * Performs the operation and returns the success message.
     */
    protected abstract String run(C controller, String[] fields);
}
