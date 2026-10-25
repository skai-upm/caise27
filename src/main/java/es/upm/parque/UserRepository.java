package es.upm.parque;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line with fields separated by ';'.
 */
public class UserRepository {

    public static final String FILE_NAME = "users.txt";

    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet.
     * Malformed lines are skipped with a warning.
     */
    public List<User> load() {
        List<User> users = new ArrayList<>();
        for (String[] fields : TextStore.readRecords(file)) {
            if (fields.length != FIELD_COUNT || !UserValidator.isValidDni(fields[2])) {
                TextStore.warnMalformed(file, fields);
                continue;
            }
            users.add(new User(fields[0], fields[1], fields[2], fields[3],
                    fields[4], fields[5], fields[6]));
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    public void saveAll(Collection<User> users) {
        TextStore.writeLines(file, users.stream()
                .map(user -> String.join(TextStore.SEPARATOR, user.getName(), user.getSurnames(), user.getDni(),
                        user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()))
                .toList());
    }
}
