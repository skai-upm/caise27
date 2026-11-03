package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.ticket.Pass;

import java.util.List;

/**
 * Outcome of a successful entry: who entered and which passes were consumed.
 */
public record EntryResult(List<User> admitted, List<Pass> consumedPasses) {
}
