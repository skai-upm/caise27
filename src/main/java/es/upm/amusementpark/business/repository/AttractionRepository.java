package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Attraction;

import java.util.Collection;
import java.util.List;

/** Replaceable storage of the park's attractions. */
public interface AttractionRepository {

    void saveAll(Collection<Attraction<?>> attractions);

    /**
     * Rebuilds the stored attractions into the park through its reconstruction methods.
     *
     * @param skipped receives stored entries that could not be read
     */
    void loadInto(ParkController park, List<String> skipped);
}
