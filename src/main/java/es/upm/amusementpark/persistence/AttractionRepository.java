package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import java.util.List;

/**
 * Park attractions (identity, name, capacity and type; never their occupants).
 */
public interface AttractionRepository {

    List<Attraction<?>> findAll();

    Attraction<?> findById(String id);

    void add(Attraction<?> attraction);
}
