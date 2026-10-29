package es.upm.amusementpark.business.repository;

/**
 * The repository instances configured for one execution, injected into the controllers at start-up.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           AttractionRepository attractions, PassRepository passes) {
}
