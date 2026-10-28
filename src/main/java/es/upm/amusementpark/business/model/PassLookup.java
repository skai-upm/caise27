package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Finds the passes bought for an individual ticket.
 */
@FunctionalInterface
public interface PassLookup {

    List<Pass> passesOf(IndividualTicket ticket);
}
