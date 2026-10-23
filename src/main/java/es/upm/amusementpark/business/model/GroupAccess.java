package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Capability of a ticket that admits a whole group through the individual
 * tickets it is composed of.
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getComposition();
}
