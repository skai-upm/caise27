package com.amusementpark.business.model;

import java.util.List;

/**
 * Outcome of an attraction entry: either a rejection reason, or the users admitted and passes consumed.
 */
public record EntryResult(String error, List<User> admitted, List<Pass> consumedPasses) {

    public static EntryResult rejected(String reason) {
        return new EntryResult(reason, List.of(), List.of());
    }

    public static EntryResult admitted(List<User> users, List<Pass> passes) {
        return new EntryResult(null, List.copyOf(users), List.copyOf(passes));
    }

    public boolean isAdmitted() {
        return error == null;
    }
}
