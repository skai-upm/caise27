package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.attraction.Attraction;
import java.util.List;

/**
 * Persistent store of attractions (without occupants).
 */
public interface AttractionRepository {

    List<Attraction<?>> load();

    void save(List<Attraction<?>> attractions);
}
