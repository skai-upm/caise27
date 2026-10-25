package es.upm.parque.business.model;

import java.util.List;

/**
 * Outcome of an attraction entry: either an error, or success with the passes consumed.
 */
public record EntryResult(String error, int users, List<Pass> consumedPasses) {

    public static EntryResult rejected(String error) {
        return new EntryResult(error, 0, List.of());
    }

    public static EntryResult admitted(int users, List<Pass> consumedPasses) {
        return new EntryResult(null, users, List.copyOf(consumedPasses));
    }

    public boolean isAdmitted() {
        return error == null;
    }
}
