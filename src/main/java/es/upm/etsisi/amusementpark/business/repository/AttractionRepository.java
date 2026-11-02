package es.upm.etsisi.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;

/**
 * Stores and retrieves attractions (never their occupants). Implementations are chosen and injected at start-up.
 */
public interface AttractionRepository {
    List<Attraction<?>> load() throws IOException;

    void save(List<Attraction<?>> attractions) throws IOException;

    /**
     * Problems found by the last load (skipped records).
     */
    List<String> getWarnings();
}
