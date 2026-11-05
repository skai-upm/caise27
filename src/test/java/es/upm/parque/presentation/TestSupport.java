package es.upm.parque.presentation;

import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.UserRepository;

/**
 * Test helper: initializes the application with in-memory repositories holding the given users and returns a
 * processor for it.
 */
public final class TestSupport {

    private TestSupport() {
    }

    public static CommandProcessor processor(UserRepository users) {
        ParkConfiguration.initialize(ParkRepositories.inMemory(users));
        return new CommandProcessor();
    }
}
