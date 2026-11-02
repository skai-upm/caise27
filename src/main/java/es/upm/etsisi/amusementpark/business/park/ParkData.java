package es.upm.etsisi.amusementpark.business.park;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.util.List;

/**
 * Snapshot of the persistent park state exchanged with a {@link ParkRepository}.
 */
public record ParkData(List<User> users, List<Group> groups, List<Ticket> tickets, List<Pass> passes,
                       List<Attraction<?>> attractions) {
}
