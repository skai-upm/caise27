package es.upm.etsisi.amusementpark.business.attraction;

import java.util.List;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * The users admitted by one entry and the passes it consumed.
 */
public record EntryResult(List<User> users, List<Pass> consumedPasses) {
}
