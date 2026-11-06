package es.upm.park.business.model;

import java.util.List;

/**
 * Gives the passes bought for an individual ticket.
 */
@FunctionalInterface
public interface PassLookup {

    List<Pass> passesOf(IndividualAccess ticket);
}
