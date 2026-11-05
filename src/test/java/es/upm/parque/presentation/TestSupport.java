package es.upm.parque.presentation;

import es.upm.parque.business.BCryptPasswordProtector;
import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.business.PasswordProtector;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.UserRepository;

/**
 * Test helper: initializes the application with in-memory repositories holding the given users and returns a
 * processor for it.
 */
public final class TestSupport {

    /** BCrypt with the minimum cost, so the test suite stays fast; production uses the default cost. */
    public static final PasswordProtector FAST_PASSWORDS = new BCryptPasswordProtector(4);

    private TestSupport() {
    }

    public static CommandProcessor processor(UserRepository users) {
        ParkConfiguration.initialize(ParkRepositories.inMemory(users), FAST_PASSWORDS);
        return new CommandProcessor();
    }
}
