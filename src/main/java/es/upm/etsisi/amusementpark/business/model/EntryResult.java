package es.upm.etsisi.amusementpark.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry.
 */
public record EntryResult(List<User> entered, List<Pass> consumedPasses) {
}
