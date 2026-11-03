package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.Pass;
import java.util.List;

/** Outcome of a successful attraction entry: the attraction, how many users entered and the passes consumed. */
public class EntryResult {

    private final Attraction<?> attraction;
    private final int admitted;
    private final List<Pass> consumed;

    public EntryResult(Attraction<?> attraction, int admitted, List<Pass> consumed) {
        this.attraction = attraction;
        this.admitted = admitted;
        this.consumed = List.copyOf(consumed);
    }

    public Attraction<?> getAttraction() {
        return attraction;
    }

    public int getAdmitted() {
        return admitted;
    }

    public List<Pass> getConsumed() {
        return consumed;
    }
}
