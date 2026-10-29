package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.model.Pass;
import java.util.List;

/**
 * Outcome of a successful attraction entry: how many users entered and which passes were consumed.
 */
public record EntryResult(int users, List<Pass> consumedPasses) {

    public EntryResult {
        consumedPasses = List.copyOf(consumedPasses);
    }
}
