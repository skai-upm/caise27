package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General admission capability shared by every ticket: it can be validated
 * for a date and tells which users it admits.
 */
public interface Admission {

    String getId();

    boolean isValidOn(LocalDate today);

    List<User> holders();
}
