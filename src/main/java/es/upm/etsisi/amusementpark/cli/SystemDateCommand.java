package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

import java.io.PrintStream;

/**
 * SYSTEM DATE;&lt;yyyy-MM-dd&gt;: replaces the current system date.
 */
public class SystemDateCommand implements Command {

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        if (arguments.length != 1) {
            out.println("ERROR: Usage: SYSTEM DATE;<yyyy-MM-dd>");
            return false;
        }
        try {
            ParkController controller = ParkController.getInstance();
            controller.setCurrentDate(arguments[0].trim());
            out.println("SYSTEM DATE UPDATED: " + controller.getCurrentDate());
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return false;
    }
}
