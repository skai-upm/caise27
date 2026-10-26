package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of giving access to attractions: something valid for a date that admits a set of users.
 */
public interface AttractionAccess {

    String getId();

    LocalDate getDate();

    /** The users admitted with this access. */
    List<User> getUsers();
}
