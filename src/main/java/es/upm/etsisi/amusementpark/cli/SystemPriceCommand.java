package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

import java.io.PrintStream;

/**
 * SYSTEM PRICE;&lt;Price&gt;: replaces the current base ticket price.
 */
public class SystemPriceCommand implements Command {

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        if (arguments.length != 1) {
            out.println("ERROR: Usage: SYSTEM PRICE;<Price>");
            return false;
        }
        try {
            ParkController controller = ParkController.getInstance();
            controller.setCurrentPrice(arguments[0].trim());
            out.println("SYSTEM PRICE UPDATED: " + controller.getCurrentPrice());
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return false;
    }
}
