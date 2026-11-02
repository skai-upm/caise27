package es.upm.etsisi.amusementpark.business.park;

import es.upm.etsisi.amusementpark.business.StorageException;

/**
 * Replaceable persistence of the park state. Implementations are created at start-up and
 * injected; they are not singletons.
 */
public interface ParkRepository {

    ParkData load() throws StorageException;

    /** Saves the whole state; on failure the previously stored state must remain intact. */
    void save(ParkData data) throws StorageException;
}
