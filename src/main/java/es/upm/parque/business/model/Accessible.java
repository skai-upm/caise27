package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of being used to access an attraction: it is valid for one date and admits some users.
 */
public interface Accessible {

    String getId();

    LocalDate getDate();

    List<User> getHolders();
}
