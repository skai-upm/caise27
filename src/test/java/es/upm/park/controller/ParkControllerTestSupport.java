package es.upm.park.controller;

/** Gives tests in other packages access to the startup-state reset. */
public final class ParkControllerTestSupport {

    private ParkControllerTestSupport() {
    }

    public static void reset() {
        ParkController.getInstance().reset();
    }
}
