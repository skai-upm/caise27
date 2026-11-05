package es.upm.amusementpark.business;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of any ticket that can be used to enter a general attraction.
 */
public interface GeneralAccess {

    String getId();

    LocalDate getDate();

    /** @return e-mails of the users admitted together with this ticket */
    List<String> holders();

    /** @return identifiers of the individual tickets involved (itself, or the components of a group ticket) */
    List<String> individualTicketIds();
}
