package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.repository.UserRepository;
import es.upm.etsisi.amusementpark.business.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users, one per line: {@code dni;email;passwordHash}. Only the BCrypt value of a
 * password is stored (empty when the user has none); {@code dni;email} lines of earlier
 * versions are still read.
 */
public class TextUserRepository extends TextFileRepository implements UserRepository {

    public TextUserRepository(Path file) {
        super(file);
    }

    @Override
    public void save(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(join(user.getDni(), user.getEmail(), user.hasPassword() ? user.getPasswordHash() : ""));
        }
        writeLines(lines);
    }

    @Override
    public List<User> load() {
        List<User> users = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f.length < 2 || f.length > 3 || f[0].isEmpty() || f[1].isEmpty()) {
                problem(i + 1, line, "expected dni;email;passwordHash");
                continue;
            }
            users.add(new User(f[0], f[1], f.length == 3 && !f[2].isEmpty() ? f[2] : null));
        }
        return users;
    }
}
