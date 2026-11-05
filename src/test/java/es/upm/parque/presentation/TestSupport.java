package es.upm.parque.presentation;

import es.upm.parque.business.ParkController;
import es.upm.parque.persistence.UserRepository;

/**
 * Test helper: starts a fresh in-memory park execution with the given users and returns a processor for it.
 */
public final class TestSupport {

    private TestSupport() {
    }

    public static CommandProcessor processor(UserRepository users) {
        ParkController.getInstance().startExecution(users);
        return new CommandProcessor();
    }
}
