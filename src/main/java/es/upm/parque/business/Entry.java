package es.upm.parque.business;

import java.util.List;

import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

/**
 * Result of a successful attraction entry: the users who entered and the passes consumed by the entry.
 */
public record Entry(List<User> entrants, List<Pass> consumed) {

    public Entry {
        entrants = List.copyOf(entrants);
        consumed = List.copyOf(consumed);
    }
}
