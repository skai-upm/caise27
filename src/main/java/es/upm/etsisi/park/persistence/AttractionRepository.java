package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.attraction.Attraction;

import java.util.Optional;

/**
 * Park attractions of any type. Occupants are execution state and are never persisted.
 */
public interface AttractionRepository extends Repository<Attraction<?>> {

    /** The attraction the user is currently inside, if any. */
    Optional<Attraction<?>> findContaining(User user);
}
