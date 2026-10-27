package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Gives access to the passes of an individual ticket without coupling the model to storage.
 */
@FunctionalInterface
public interface PassLookup {

    List<Pass> passesOf(IndividualTicket ticket);
}
