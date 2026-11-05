package es.upm.parque.business;

import es.upm.parque.persistence.ParkRepositories;

/**
 * Composition root of the application, written without any dependency injection framework: during
 * initialization it injects the configured repositories into the central park management and wires every
 * singleton controller with the collaborators it needs.
 */
public final class ParkConfiguration {

    static final String NOT_INITIALIZED = "the application has not been initialized";

    private ParkConfiguration() {
    }

    /** Starts an execution with the given repositories and BCrypt password protection. */
    public static ParkController initialize(ParkRepositories repositories) {
        return initialize(repositories, new BCryptPasswordProtector());
    }

    /** Starts an execution with the given repositories and injects the dependencies of every controller. */
    public static ParkController initialize(ParkRepositories repositories, PasswordProtector passwords) {
        ParkController park = ParkController.getInstance();
        park.startExecution(repositories);
        TicketManager tickets = TicketManager.getInstance();
        tickets.inject(park);
        UserManager.getInstance().inject(park, passwords);
        GroupManager.getInstance().inject(park);
        SystemManager.getInstance().inject(park);
        InvoiceManager.getInstance().inject(park);
        PassManager.getInstance().inject(park);
        AttractionManager.getInstance().inject(park, tickets);
        return park;
    }

    /** Returns the injected dependency, or fails clearly when the application has not been initialized. */
    static <T> T requireInjected(T dependency) {
        if (dependency == null) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        return dependency;
    }
}
