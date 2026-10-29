package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.repository.UserRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file: one user per line, attributes separated by ';'.
 */
public class TextUserRepository implements UserRepository {

    private static final int FIELD_COUNT = 7;

    private final Path file;

    public TextUserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet; malformed lines are skipped.
     */
    @Override
    public List<User> load() {
        List<User> users = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != FIELD_COUNT) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /**
     * Replaces the stored users with the given list.
     */
    @Override
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(TextFiles.SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getPhone(), u.getCard()));
        }
        TextFiles.writeLines(file, lines);
    }
}
