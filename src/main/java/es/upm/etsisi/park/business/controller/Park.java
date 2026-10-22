package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.Repositories;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Central park management. Exactly one instance exists per execution; it owns the system
 * state and wires the controllers with the repositories and collaborators created at
 * application initialization (dependency injection without a framework).
 */
public final class Park {

    private static final Park INSTANCE = new Park();

    private SystemState state;
    private Repositories repositories;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Configures the park for an execution: the system date is reset to today, the base
     * price to 40.00, and every controller receives its dependencies.
     */
    public void configure(Repositories repositories, PasswordHasher hasher) {
        this.repositories = repositories;
        this.state = new SystemState(LocalDate.now(ZoneId.systemDefault()));
        PassController passController = PassController.getInstance();

        SystemController.getInstance().configure(state);
        UserController.getInstance().configure(repositories.users(), repositories.groups(), repositories.tickets(),
                repositories.attractions(), state, hasher);
        GroupController.getInstance().configure(repositories.groups(), repositories.users(), repositories.tickets(), state);
        TicketController.getInstance().configure(repositories.tickets(), repositories.users(), repositories.groups(),
                repositories.passes(), state);
        passController.configure(repositories.passes(), repositories.tickets(), state);
        AttractionController.getInstance().configure(repositories.attractions(), repositories.tickets(),
                repositories.users(), repositories.passes(), passController, state);
        InvoiceController.getInstance().configure(repositories.users(), repositories.groups(), repositories.tickets(),
                repositories.passes());
    }

    public SystemState getState() {
        return Checks.configured(state, "Park");
    }

    public Repositories getRepositories() {
        return Checks.configured(repositories, "Park");
    }

    public SystemController getSystemController() {
        return SystemController.getInstance();
    }

    public UserController getUserController() {
        return UserController.getInstance();
    }

    public GroupController getGroupController() {
        return GroupController.getInstance();
    }

    public TicketController getTicketController() {
        return TicketController.getInstance();
    }

    public PassController getPassController() {
        return PassController.getInstance();
    }

    public AttractionController getAttractionController() {
        return AttractionController.getInstance();
    }

    public InvoiceController getInvoiceController() {
        return InvoiceController.getInstance();
    }
}
