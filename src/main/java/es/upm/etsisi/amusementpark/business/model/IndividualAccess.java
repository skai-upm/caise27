package es.upm.etsisi.amusementpark.business.model;

import java.util.Optional;

/** Access capability of a single person. */
public interface IndividualAccess extends AttractionAccess {

    /** The person this access belongs to, if they still exist. */
    Optional<User> getUser();
}
