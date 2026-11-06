package es.upm.park.business.controller;

/** Gives tests in other packages access to the startup-state reset. */
public final class ParkControllerTestSupport {

    private ParkControllerTestSupport() {
    }

    /** Restores the startup state and wires the controllers as the application configuration does. */
    public static void reset() {
        ParkController.getInstance().reset();
        InvoiceController.getInstance().setParkController(ParkController.getInstance());
    }
}
