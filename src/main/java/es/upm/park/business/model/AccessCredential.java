package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Access capability: what an attraction needs from anything used to enter it.
 */
public interface AccessCredential {

    String getId();

    /** Date for which the credential is valid. */
    LocalDate getDate();

    /** Users admitted when the credential is used. */
    List<User> getHolders();
}
