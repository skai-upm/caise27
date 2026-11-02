package es.upm.etsisi.amusementpark.business.repository;

/**
 * The set of repositories injected into the park controller at initialization.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           AttractionRepository attractions, PassRepository passes) {
}
