package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.PrintStream;

/**
 * SYSTEM PRICE;&lt;Price&gt;: replaces the current base ticket price.
 */
public class SystemPriceCommand implements Command {

    private final ParkController controller;

    public SystemPriceCommand(ParkController controller) {
        this.controller = controller;
    }

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        if (arguments.length != 1) {
            out.println("ERROR: Usage: SYSTEM PRICE;<Price>");
            return false;
        }
        try {
            controller.setCurrentPrice(arguments[0].trim());
            out.println("SYSTEM PRICE UPDATED: " + controller.getCurrentPrice());
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return false;
    }
}
