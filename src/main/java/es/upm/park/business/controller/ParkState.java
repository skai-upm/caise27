package es.upm.park.business.controller;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.Ticket;

import java.util.List;

/**
 * Snapshot of the persistent park state. {@code tickets} contains only top-level
 * tickets (individual tickets bought on their own and group tickets); composed
 * tickets travel inside their group ticket. Attraction occupants are not part of it.
 */
public record ParkState(List<Group> groups, List<Ticket> tickets, List<Attraction> attractions, List<Pass> passes) {

    public ParkState(List<Group> groups, List<Ticket> tickets, List<Attraction> attractions) {
        this(groups, tickets, attractions, List.of());
    }

    public static ParkState empty() {
        return new ParkState(List.of(), List.of(), List.of(), List.of());
    }
}
