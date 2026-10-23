package es.upm.park.persistence;

/**
 * The set of repositories injected into the park at start-up. Any
 * implementation or configuration can be supplied.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           AttractionRepository attractions, PassRepository passes) {
}
