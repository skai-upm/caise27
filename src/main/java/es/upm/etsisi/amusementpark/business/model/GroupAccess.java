package es.upm.etsisi.amusementpark.business.model;

import java.util.List;

/** Access capability of a whole group, made of one individual ticket per member. */
public interface GroupAccess extends AttractionAccess {

    List<IndividualTicket> getParts();
}
