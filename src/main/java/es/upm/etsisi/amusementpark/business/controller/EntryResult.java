package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;

import java.util.List;

/** Outcome of a successful attraction entry: the ticket used and the passes consumed. */
public record EntryResult(Ticket ticket, List<Pass> consumedPasses) {
}
