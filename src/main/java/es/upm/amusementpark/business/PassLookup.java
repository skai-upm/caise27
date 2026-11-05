package es.upm.amusementpark.business;

import java.util.List;

/**
 * Lookup of the passes associated with an individual ticket, provided by central management.
 */
@FunctionalInterface
public interface PassLookup {

    List<Pass> passesOfTicket(String ticketId);
}
