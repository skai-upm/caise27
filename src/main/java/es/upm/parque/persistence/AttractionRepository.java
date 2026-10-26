package es.upm.parque.persistence;

import es.upm.parque.business.model.Attraction;
import java.util.Collection;
import java.util.List;

/**
 * Stores and restores attraction definitions.
 */
public interface AttractionRepository {

    List<Attraction> load();

    void save(Collection<Attraction> attractions);
}
