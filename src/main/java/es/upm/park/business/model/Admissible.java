package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.List;

/** Access capability shared by every ticket: what an attraction needs to admit its holders. */
public interface Admissible {

    String getId();

    LocalDate getDate();

    /** Users admitted with this ticket; {@code null} entries stand for deleted users. */
    List<User> getHolders();
}
