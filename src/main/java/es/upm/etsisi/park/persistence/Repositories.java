package es.upm.etsisi.park.persistence;

/**
 * The set of repositories used by one execution, created at initialization and injected
 * into the controllers.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           AttractionRepository attractions, PassRepository passes) {
}
