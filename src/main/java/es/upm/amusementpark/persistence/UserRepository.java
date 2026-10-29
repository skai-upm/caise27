package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Persists users as {@code email;dni} lines. */
public class UserRepository extends TextFileRepository {

    public UserRepository(Path file) {
        super(file);
    }

    public void saveAll(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(join(user.getEmail(), user.getDni()));
        }
        writeLines(lines);
    }

    /** @param skipped receives each line that could not be read */
    public List<User> loadAll(List<String> skipped) {
        List<User> users = new ArrayList<>();
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f.length == 2 && !f[0].isEmpty()) {
                users.add(new User(f[0], f[1]));
            } else {
                skipped.add(line);
            }
        }
        return users;
    }
}
