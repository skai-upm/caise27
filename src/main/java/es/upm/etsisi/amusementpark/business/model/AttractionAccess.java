package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/** Capability of being used to enter an attraction: a dated pass for some visitors. */
public interface AttractionAccess {

    String getId();

    LocalDate getDate();

    /** The users admitted by this access. */
    List<User> getVisitors();
}
