package es.upm.parque.business.model;

import java.util.List;

/**
 * Read access to the passes of individual tickets, as needed by attraction entry rules.
 */
public interface PassLookup {

    /** Passes associated with the ticket, in purchase order. */
    List<Pass> passesOf(IndividualTicket ticket);

    /** The Special Pass of the ticket, consumed or not, or {@code null} when it has none. */
    SpecialPass specialPassOf(IndividualTicket ticket);
}
