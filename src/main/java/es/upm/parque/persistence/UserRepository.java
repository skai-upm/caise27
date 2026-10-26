package es.upm.parque.persistence;

import es.upm.parque.business.model.User;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with attributes separated by ';'.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads every stored user. A missing file means there are no users; malformed lines are skipped.
     */
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length == FIELD_COUNT) {
                users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
            }
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    public void saveAll(List<User> users) {
        TextFiles.writeLines(file, users.stream()
                .map(u -> String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(), u.getEmail(),
                        u.getPasswordHash(), u.getPhone(), u.getCard()))
                .toList());
    }
}
