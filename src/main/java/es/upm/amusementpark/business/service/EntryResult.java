package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.User;

import java.util.List;

/**
 * Outcome of a successful attraction entry: the users who entered and the
 * passes consumed by it.
 */
public record EntryResult(List<User> entered, List<Pass> consumedPasses) {

    public EntryResult {
        entered = List.copyOf(entered);
        consumedPasses = List.copyOf(consumedPasses);
    }
}
