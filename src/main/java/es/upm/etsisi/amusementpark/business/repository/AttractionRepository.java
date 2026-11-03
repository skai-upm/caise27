package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;

import java.util.List;

/**
 * Stores and retrieves attractions (never their occupants).
 */
public interface AttractionRepository {

    List<Attraction> load() throws StorageException;

    void save(List<Attraction> attractions) throws StorageException;
}
