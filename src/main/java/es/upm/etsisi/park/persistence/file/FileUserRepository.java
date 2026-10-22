package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Users stored in a text file through a {@link UserFileStore}.
 */
public class FileUserRepository extends ListRepository<User> implements UserRepository {

    private final UserFileStore store;

    public FileUserRepository(UserFileStore store) {
        super(store.load(), User::getDni);
        this.store = store;
    }

    @Override
    protected void persist(List<User> current) {
        store.save(current);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        for (User user : items) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByDni(String dni) {
        for (User user : items) {
            if (user.getDni().equalsIgnoreCase(dni)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
