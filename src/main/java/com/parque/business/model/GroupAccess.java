package com.parque.business.model;

import java.util.List;

/**
 * Access capability of a group ticket: it admits the owners of its constituent individual tickets.
 */
public interface GroupAccess extends Admission {
    List<IndividualTicket> getConstituents();
}
