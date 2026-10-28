package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Outcome of a successful attraction entry: users who entered and passes consumed.
 */
public record EntryResult(List<User> users, List<Pass> consumed) {

    public EntryResult {
        users = List.copyOf(users);
        consumed = List.copyOf(consumed);
    }
}
