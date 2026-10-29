package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Pass;

import java.util.Collection;
import java.util.List;

/** Replaceable storage of the park's SpeedPasses and Special Passes. */
public interface PassRepository {

    void saveAll(Collection<Pass> passes);

    /**
     * Rebuilds the stored passes into the park; must run after tickets are loaded.
     *
     * @param skipped receives stored entries that could not be read
     */
    void loadInto(ParkController park, List<String> skipped);
}
