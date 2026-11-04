package es.upm.park.business.model;

import java.util.List;

/** Access capability of a group ticket: it admits the owners of its constituent tickets. */
public interface GroupAccess extends Admissible {

    List<IndividualTicket> getConstituents();
}
