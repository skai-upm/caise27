package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Attraction;
import java.util.Collection;
import java.util.List;

/**
 * Storage for attractions (without their occupants).
 */
public interface AttractionRepository {

    List<Attraction<?>> loadAll();

    void saveAll(Collection<Attraction<?>> attractions);
}
