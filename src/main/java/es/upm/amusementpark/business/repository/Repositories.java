package es.upm.amusementpark.business.repository;

import java.util.Objects;

/** The set of repositories injected into central park management. */
public record Repositories(UserRepository users, GroupRepository groups,
        TicketRepository tickets, AttractionRepository attractions, PassRepository passes) {

    public Repositories {
        Objects.requireNonNull(users);
        Objects.requireNonNull(groups);
        Objects.requireNonNull(tickets);
        Objects.requireNonNull(attractions);
        Objects.requireNonNull(passes);
    }
}
