package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Attraction;
import java.util.List;

/**
 * Repository of attractions. Implementations decide where and how the data is stored; controllers depend only
 * on this interface, so the implementation or its location can be replaced at startup.
 */
public interface AttractionRepository {

    /** Loads the stored attraction definitions (always empty of visitors). */
    List<Attraction> load();

    /** Stores exactly the given attraction definitions. */
    void save(List<Attraction> attractions);
}
