package com.amusementpark.persistence;

import com.amusementpark.business.model.Attraction;
import java.io.IOException;
import java.util.List;

/**
 * Storage of attraction definitions.
 */
public interface AttractionRepository {

    List<Attraction<?>> load() throws IOException;

    void save(List<Attraction<?>> attractions) throws IOException;
}
