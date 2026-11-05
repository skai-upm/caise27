package es.upm.parque.business.model;

/**
 * Read access to the passes of individual tickets, as needed by attraction entry rules.
 */
public interface PassLookup {

    /** The Special Pass of the ticket, consumed or not, or {@code null} when it has none. */
    SpecialPass specialPassOf(IndividualTicket ticket);
}
