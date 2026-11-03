package es.upm.etsisi.park;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line, attributes separated by ';'.
 */
public class UserRepository extends TextFileRepository {

    private static final int FIELDS = 7;

    public UserRepository(Path file) {
        super(file);
    }

    /**
     * Loads all stored users. A missing file means there are no users yet.
     * Lines that do not have exactly seven fields are skipped with a warning.
     */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        for (String line : readLines()) {
            String[] f = fields(line, FIELDS);
            if (f.length == 0) {
                warnMalformed(line);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    public void saveAll(Collection<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(toLine(u));
        }
        writeLines(lines);
    }

    static String toLine(User u) {
        return String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(),
                u.getEmail(), u.getPassword(), u.getPhone(), u.getCard());
    }
}
