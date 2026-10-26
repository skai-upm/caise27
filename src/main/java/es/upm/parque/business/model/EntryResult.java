package es.upm.parque.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry.
 *
 * @param admitted number of users who entered
 * @param consumed passes consumed by the entry, in order
 */
public record EntryResult(int admitted, List<Pass> consumed) {

    public EntryResult {
        consumed = List.copyOf(consumed);
    }
}
