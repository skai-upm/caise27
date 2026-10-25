package es.upm.parque;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with its attributes separated by ';', and loads them back.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final TextFileStore store;

    public UserRepository(Path file) {
        this.store = new TextFileStore(file);
    }

    /**
     * Loads every stored user. A missing file means there are no users; malformed lines are skipped.
     */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        for (String line : store.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                store.warnSkipped(line, "expected " + FIELDS + " fields");
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /**
     * Replaces the file content with the given users.
     */
    public void saveAll(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(SEPARATOR, user.getName(), user.getSurname(), user.getDni(), user.getEmail(),
                    user.getPassword(), user.getPhone(), user.getCard()));
        }
        store.writeLines(lines);
    }
}
