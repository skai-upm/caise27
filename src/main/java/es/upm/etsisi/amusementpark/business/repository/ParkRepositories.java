package es.upm.etsisi.amusementpark.business.repository;

/**
 * The repositories injected into the park controller at start-up.
 */
public record ParkRepositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                               AttractionRepository attractions, PassRepository passes) {
}
