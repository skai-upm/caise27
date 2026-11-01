package es.upm.amusementpark.business.controller;

import java.util.List;

import es.upm.amusementpark.business.model.Pass;

/**
 * Result of a successful attraction entry: how many users entered and which
 * passes were consumed.
 */
public record AttractionEntry(int entered, List<Pass> consumedPasses) {

    public AttractionEntry {
        consumedPasses = List.copyOf(consumedPasses);
    }
}
