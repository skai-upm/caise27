package es.upm.parque.business.model;

import java.util.List;

/**
 * Outcome of checking the pass conditions of an entry: either the passes to consume when the entry completes, or
 * the reason why the entry is refused.
 */
public record PassSelection(List<Pass> passes, String refusal) {

    public PassSelection {
        passes = List.copyOf(passes);
    }

    public static PassSelection ok(List<Pass> passes) {
        return new PassSelection(passes, null);
    }

    public static PassSelection refused(String reason) {
        return new PassSelection(List.of(), reason);
    }

    public boolean isRefused() {
        return refusal != null;
    }
}
