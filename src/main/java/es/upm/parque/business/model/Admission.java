package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Something that lets users into an attraction. Attractions handle every admission the same way,
 * whatever concrete ticket type provides it.
 */
public interface Admission {

    /** @return the registered users this admission lets in */
    List<User> getAdmittedUsers();

    /** @return true if the admission can be used on {@code day} */
    boolean isValidOn(LocalDate day);
}
