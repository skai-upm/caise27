package es.upm.etsisi.park.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Generic store of domain objects of type {@code T}. Implementations decide where and how
 * the objects are persisted; callers only depend on this contract.
 *
 * @param <T> the stored domain type
 */
public interface Repository<T> {

    Optional<T> findById(String id);

    List<T> findAll();

    /** Adds the item and persists the change. */
    void add(T item);

    /** Removes the item and persists the change. */
    void remove(T item);

    /** Persists the current state, e.g. after a stored object has been modified. */
    void save();
}
