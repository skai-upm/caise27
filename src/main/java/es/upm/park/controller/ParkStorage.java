package es.upm.park.controller;

/** Where the persistent park state is kept between executions. */
public interface ParkStorage {

    ParkState load();

    void save(ParkState state);

    /** Storage that keeps nothing: the default when no persistence is configured. */
    ParkStorage NONE = new ParkStorage() {
        @Override
        public ParkState load() {
            return ParkState.empty();
        }

        @Override
        public void save(ParkState state) {
            // nothing to keep
        }
    };
}
