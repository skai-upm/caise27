package es.upm.etsisi.park.persistence;

/**
 * The repositories an application run uses, created at start-up and injected
 * into the controllers. Any implementation of the interfaces can be supplied.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           PassRepository passes, AttractionRepository attractions) {
}
