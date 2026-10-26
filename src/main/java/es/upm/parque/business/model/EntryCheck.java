package es.upm.parque.business.model;

import java.util.List;

/**
 * Outcome of checking the pass rules of an attraction entry: either the passes to consume
 * when the entry succeeds, or the reason it is rejected.
 */
public final class EntryCheck {

    private final String rejection;
    private final List<PersonalPass> passes;

    private EntryCheck(String rejection, List<PersonalPass> passes) {
        this.rejection = rejection;
        this.passes = passes;
    }

    public static EntryCheck passes(List<PersonalPass> passes) {
        return new EntryCheck(null, List.copyOf(passes));
    }

    public static EntryCheck rejected(String reason) {
        return new EntryCheck(reason, List.of());
    }

    public boolean isRejected() {
        return rejection != null;
    }

    public String getRejection() {
        return rejection;
    }

    public List<PersonalPass> getPasses() {
        return passes;
    }
}
