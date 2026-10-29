package es.upm.amusementpark.business.model;

import java.util.List;

/** Result of a successful attraction entry: who entered and which passes were consumed. */
public record Entry(List<User> entrants, List<Pass> consumedPasses) {

    public Entry {
        entrants = List.copyOf(entrants);
        consumedPasses = List.copyOf(consumedPasses);
    }
}
