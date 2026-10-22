package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.persistence.AttractionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Attractions stored in a text file through an {@link AttractionFileStore}.
 */
public class FileAttractionRepository extends ListRepository<Attraction<?>> implements AttractionRepository {

    private final AttractionFileStore store;

    public FileAttractionRepository(AttractionFileStore store) {
        super(store.load(), Attraction::getId);
        this.store = store;
    }

    @Override
    protected void persist(List<Attraction<?>> current) {
        store.save(current);
    }

    @Override
    public Optional<Attraction<?>> findContaining(User user) {
        for (Attraction<?> attraction : items) {
            if (attraction.isInside(user)) {
                return Optional.of(attraction);
            }
        }
        return Optional.empty();
    }
}
