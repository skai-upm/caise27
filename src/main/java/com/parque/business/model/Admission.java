package com.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Access capability of any ticket: it is valid for a date and admits some users.
 */
public interface Admission {
    String getId();

    LocalDate getDate();

    /** Users admitted with this ticket. */
    List<User> getVisitors();
}
