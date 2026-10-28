package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.PrintStream;

/**
 * SYSTEM DATE;&lt;yyyy-MM-dd&gt;: replaces the current system date.
 */
public class SystemDateCommand implements Command {

    private final ParkController controller;

    public SystemDateCommand(ParkController controller) {
        this.controller = controller;
    }

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        if (arguments.length != 1) {
            out.println("ERROR: Usage: SYSTEM DATE;<yyyy-MM-dd>");
            return false;
        }
        try {
            controller.setCurrentDate(arguments[0].trim());
            out.println("SYSTEM DATE UPDATED: " + controller.getCurrentDate());
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return false;
    }
}
