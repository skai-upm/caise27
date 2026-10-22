package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test stand-in that keeps saved users in memory and can be told to fail.
 */
public class InMemoryUserRepository implements UserRepository {

    private final List<User> saved = new ArrayList<>();
    private boolean failOnSave;
    private int saveCount;

    public InMemoryUserRepository(User... initial) {
        for (User user : initial) {
            saved.add(new User(user));
        }
    }

    public void setFailOnSave(boolean failOnSave) {
        this.failOnSave = failOnSave;
    }

    public List<User> getSaved() {
        return saved;
    }

    public int getSaveCount() {
        return saveCount;
    }

    @Override
    public List<User> loadAll() {
        List<User> copy = new ArrayList<>();
        for (User user : saved) {
            copy.add(new User(user));
        }
        return copy;
    }

    @Override
    public void saveAll(Collection<User> users) throws IOException {
        if (failOnSave) {
            throw new IOException("disk full");
        }
        saveCount++;
        saved.clear();
        for (User user : users) {
            saved.add(new User(user));
        }
    }
}
