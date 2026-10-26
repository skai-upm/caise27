package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of granting access to attractions: something with an identifier, a date of validity
 * and the users it admits.
 */
public interface Admission {

    String getId();

    LocalDate getDate();

    List<User> getUsers();
}
