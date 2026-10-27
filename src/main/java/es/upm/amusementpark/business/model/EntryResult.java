package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry: who entered and which passes were consumed.
 */
public record EntryResult(List<User> users, List<Pass> consumedPasses) {

    public EntryResult {
        users = List.copyOf(users);
        consumedPasses = List.copyOf(consumedPasses);
    }
}
