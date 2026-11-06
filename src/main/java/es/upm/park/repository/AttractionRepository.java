package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.model.Attraction;

/**
 * Keeps the attractions of the park.
 */
public class AttractionRepository {

    private final List<Attraction> attractions = new ArrayList<>();

    public void add(Attraction attraction) {
        attractions.add(attraction);
    }

    public Optional<Attraction> findById(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public List<Attraction> findAll() {
        return Collections.unmodifiableList(attractions);
    }
}
