package es.upm.etsisi.park.business;

/**
 * Persistence abstraction used by the business layer. Implementations are created at start-up and
 * injected, so the storage technology or location can change without touching business rules.
 */
public interface ParkRepository {

    /** Reconstructs every persistent element; attractions are returned without occupants. */
    ParkData load();

    /**
     * Stores every persistent element.
     *
     * @throws PersistenceException if the data cannot be written; previous data must remain intact
     */
    void save(ParkData data);
}
