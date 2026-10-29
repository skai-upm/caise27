package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry: who entered and which passes were consumed.
 */
public record EntryResult(List<User> entered, List<Pass> consumedPasses) {

    public EntryResult {
        entered = List.copyOf(entered);
        consumedPasses = List.copyOf(consumedPasses);
    }
}
