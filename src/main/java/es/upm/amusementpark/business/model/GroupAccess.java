package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Access capability of a group purchase composed of individual tickets.
 */
public interface GroupAccess extends AttractionAccess {

    List<IndividualTicket> getComposition();
}
