package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Outcome of a successful attraction entry: how many users entered and which passes were consumed.
 */
public record EntryResult(int entered, List<SpeedPass> speedPasses, SpecialPass specialPass) {
}
