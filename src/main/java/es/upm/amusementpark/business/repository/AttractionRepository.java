package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.Attraction;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/** Storage of attraction definitions (never of the users present). */
public interface AttractionRepository {

    List<Attraction> loadAll() throws IOException;

    void saveAll(Collection<Attraction> attractions) throws IOException;
}
