package es.upm.park.controller;

import es.upm.park.model.Attraction;
import es.upm.park.model.Group;
import es.upm.park.model.Ticket;

import java.util.List;

/**
 * Snapshot of the persistent park state. {@code tickets} contains only top-level
 * tickets (individual tickets bought on their own and group tickets); composed
 * tickets travel inside their group ticket. Attraction occupants are not part of it.
 */
public record ParkState(List<Group> groups, List<Ticket> tickets, List<Attraction> attractions) {

    public static ParkState empty() {
        return new ParkState(List.of(), List.of(), List.of());
    }
}
