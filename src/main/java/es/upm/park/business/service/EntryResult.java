package es.upm.park.business.service;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.Pass;
import java.util.List;

/** Outcome of a successful attraction entry: how many users entered and which passes were consumed. */
public record EntryResult(Attraction<?> attraction, int admitted, List<Pass> consumed) {

    public EntryResult {
        consumed = List.copyOf(consumed);
    }
}
