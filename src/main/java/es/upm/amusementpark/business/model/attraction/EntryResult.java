package es.upm.amusementpark.business.model.attraction;

import es.upm.amusementpark.business.model.pass.Pass;

import java.util.List;

/**
 * Outcome of a successful attraction entry: how many users entered and which passes were consumed.
 */
public record EntryResult(int admitted, List<Pass> consumedPasses) {

    public EntryResult {
        consumedPasses = List.copyOf(consumedPasses);
    }
}
