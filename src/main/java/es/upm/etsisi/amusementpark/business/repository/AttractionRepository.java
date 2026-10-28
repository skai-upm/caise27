package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.Attraction;

import java.util.Collection;
import java.util.List;

/**
 * Persistent store of attractions (without occupants).
 */
public interface AttractionRepository {

    List<Attraction<?>> load();

    void save(Collection<Attraction<?>> attractions);

    List<String> getProblems();
}
