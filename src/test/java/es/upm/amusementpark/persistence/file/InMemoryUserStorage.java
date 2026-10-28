package es.upm.amusementpark.persistence.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.User;

/**
 * Test double keeping the last saved users in memory.
 */
public class InMemoryUserStorage implements UserStorage {

    private List<User> saved = new ArrayList<>();
    private boolean failing;

    @Override
    public List<User> load(List<String> warnings) {
        return new ArrayList<>(saved);
    }

    @Override
    public void save(List<User> users) throws IOException {
        if (failing) {
            throw new IOException("disk full");
        }
        saved = new ArrayList<>(users);
    }

    public List<User> saved() {
        return saved;
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }
}
