package es.upm.parque.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry.
 *
 * @param users    number of users that entered
 * @param consumed passes consumed by the entry
 */
public record EntryResult(int users, List<Pass> consumed) {
}
