package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.ticket.Ticket;
import es.upm.etsisi.amusementpark.user.User;
import java.util.List;

/**
 * Park state as loaded from storage.
 */
public record ParkData(List<User> users, List<Group> groups, List<Ticket> tickets, List<Attraction> attractions) {
}
