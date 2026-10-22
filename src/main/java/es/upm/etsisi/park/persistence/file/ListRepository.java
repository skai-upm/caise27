package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.persistence.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * In-memory list of items written through to a file after every change.
 *
 * @param <T> the stored domain type
 */
abstract class ListRepository<T> implements Repository<T> {

    protected final List<T> items;
    private final Function<T, String> idOf;

    protected ListRepository(List<T> loaded, Function<T, String> idOf) {
        this.items = new ArrayList<>(loaded);
        this.idOf = idOf;
    }

    /** Writes the given items to the backing file. */
    protected abstract void persist(List<T> current);

    @Override
    public Optional<T> findById(String id) {
        for (T item : items) {
            if (idOf.apply(item).equals(id)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void add(T item) {
        items.add(item);
        save();
    }

    @Override
    public void remove(T item) {
        items.remove(item);
        save();
    }

    @Override
    public void save() {
        persist(items);
    }
}
