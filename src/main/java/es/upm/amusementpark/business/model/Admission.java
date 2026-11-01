package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of granting access to attractions: the users admitted on a date
 * and the individual admissions that compose this one.
 */
public interface Admission {

    String getId();

    LocalDate getDate();

    /** Users admitted together by this admission. */
    List<User> getUsers();

    /** The individual admissions of each admitted user (the admission itself when it is individual). */
    List<? extends IndividualAdmission> getIndividualAdmissions();
}
