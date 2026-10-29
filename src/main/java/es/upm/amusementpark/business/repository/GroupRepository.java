package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Group;

import java.util.Collection;
import java.util.List;

/** Replaceable storage of the park's groups. */
public interface GroupRepository {

    void saveAll(Collection<Group> groups);

    /**
     * Rebuilds the stored groups into the park through its reconstruction methods.
     *
     * @param skipped receives stored entries that could not be read
     */
    void loadInto(ParkController park, List<String> skipped);
}
