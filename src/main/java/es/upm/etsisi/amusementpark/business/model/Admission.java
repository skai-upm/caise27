package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability of a ticket: it admits the users of its individual tickets on its date.
 */
public interface Admission {

    String getIdentifier();

    LocalDate getDate();

    List<IndividualTicket> getIndividualTickets();
}
