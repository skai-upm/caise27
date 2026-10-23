package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Pass;
import java.util.List;

/**
 * Outcome of a successful attraction entry.
 *
 * @param users    number of users who entered
 * @param consumed passes consumed by the entry, possibly none
 */
public record EntryResult(int users, List<Pass> consumed) {

    public EntryResult {
        consumed = List.copyOf(consumed);
    }
}
