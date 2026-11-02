package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.user.User;
import java.util.List;

/**
 * A fully validated entry: who enters and which passes are consumed.
 */
public record Entry(List<User> entrants, List<Pass> passes) {
}
