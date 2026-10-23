package es.upm.park.business.model.attraction;

import es.upm.park.business.model.User;
import es.upm.park.business.model.pass.Pass;

import java.util.List;

/**
 * Users admitted by a successful entry and the passes it consumed.
 */
public record EntryResult(List<User> entered, List<Pass> consumedPasses) {
}
