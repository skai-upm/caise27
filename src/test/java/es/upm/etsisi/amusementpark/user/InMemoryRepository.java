package es.upm.etsisi.amusementpark.user;

import es.upm.etsisi.amusementpark.persistence.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Test double recording the last saved state; can be told to fail on save. */
class InMemoryRepository implements UserRepository {

    List<User> saved = new ArrayList<>();
    int saves;
    boolean failOnSave;

    @Override
    public List<User> loadAll() {
        return new ArrayList<>(saved);
    }

    @Override
    public void saveAll(List<User> users) throws IOException {
        if (failOnSave) {
            throw new IOException("disk full");
        }
        saves++;
        saved = new ArrayList<>();
        for (User u : users) {
            saved.add(new User(u));
        }
    }
}
