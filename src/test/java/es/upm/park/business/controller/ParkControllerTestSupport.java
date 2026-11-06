package es.upm.park.business.controller;

import es.upm.park.business.security.BCryptPasswordHasher;
import es.upm.park.business.security.PasswordService;
import es.upm.park.business.validation.UserValidator;

/** Gives tests in other packages access to the startup-state reset. */
public final class ParkControllerTestSupport {

    private ParkControllerTestSupport() {
    }

    /** Restores the startup state and wires the controllers as the application configuration does. */
    public static void reset() {
        ParkController.getInstance().reset();
        InvoiceController.getInstance().setParkController(ParkController.getInstance());
        UserRegistry users = new UserRegistry();
        ParkController.getInstance().setUserDirectory(users);
        UserController.getInstance().configure(users,
                new PasswordService(new BCryptPasswordHasher(), UserValidator.passwordPolicy()));
    }
}
