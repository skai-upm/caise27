package es.upm.amusementpark.business;

import java.util.List;

/**
 * Replaceable persistence dependency of central park management. Implementations are created and configured at
 * application initialization and injected; they are not singletons.
 */
public interface ParkRepository {

    /** Repository used while none is configured: persists nothing. */
    ParkRepository NONE = new ParkRepository() {
        @Override
        public List<String> load(ParkController park) {
            return List.of();
        }

        @Override
        public void save(ParkController park) {
            // nothing to persist
        }
    };

    /**
     * Rebuilds the persisted state into {@code park}.
     *
     * @return warnings about records that could not be interpreted
     * @throws RepositoryException if the persisted state cannot be read
     */
    List<String> load(ParkController park);

    /**
     * Stores the current state of {@code park}.
     *
     * @throws RepositoryException if the state cannot be written
     */
    void save(ParkController park);
}
