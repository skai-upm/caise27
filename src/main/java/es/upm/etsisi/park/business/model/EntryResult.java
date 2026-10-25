package es.upm.etsisi.park.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry.
 *
 * @param entered  users who entered
 * @param consumed passes consumed by the entry
 */
public record EntryResult(List<User> entered, List<Pass> consumed) {
}
