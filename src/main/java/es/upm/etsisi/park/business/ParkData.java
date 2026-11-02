package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Snapshot of all persistent park elements exchanged with a {@link ParkRepository}.
 */
public record ParkData(List<User> users, List<Group> groups, List<Ticket> tickets,
                       List<Pass> passes, List<Attraction<?>> attractions) {

    public static ParkData empty() {
        return new ParkData(List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
