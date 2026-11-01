package es.upm.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;

/**
 * Storage of attraction definitions (never their occupants).
 */
public interface AttractionRepository {

    List<Attraction> load() throws IOException;

    void save(List<Attraction> attractions) throws IOException;
}
