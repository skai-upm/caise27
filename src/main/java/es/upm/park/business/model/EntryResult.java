package es.upm.park.business.model;

import java.util.List;

/** Users who entered an attraction and the passes consumed by that entry. */
public record EntryResult(List<String> entrants, List<String> consumedPasses) {
}
