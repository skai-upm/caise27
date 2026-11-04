package com.parque.business.controller;

import com.parque.business.model.Pass;
import java.util.List;

/**
 * Outcome of a successful attraction entry: how many users entered and which passes were consumed.
 */
public record EntryResult(int entered, List<Pass> consumedPasses) {
    public EntryResult {
        consumedPasses = List.copyOf(consumedPasses);
    }
}
