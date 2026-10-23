package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.util.List;

import es.upm.etsisi.park.business.model.attraction.Attraction;

/** Storage of attractions (identity, name, capacity and type; never occupants). */
public interface AttractionRepository {

    List<Attraction<?>> load() throws IOException;

    void save(List<Attraction<?>> attractions) throws IOException;
}
