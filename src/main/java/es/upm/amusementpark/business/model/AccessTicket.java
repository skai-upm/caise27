package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/** Capability of a ticket to give access to attractions. */
public interface AccessTicket {

    String getIdentifier();

    LocalDate getDate();

    /** Users that enter together when the ticket is used. */
    List<User> admittedUsers();
}
