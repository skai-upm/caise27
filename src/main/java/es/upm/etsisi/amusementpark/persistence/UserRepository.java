package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users, one per line: {@code dni;email}.
 */
public class UserRepository extends TextFileRepository {

    public UserRepository(Path file) {
        super(file);
    }

    public void save(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(join(user.getDni(), user.getEmail()));
        }
        writeLines(lines);
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f.length != 2 || f[0].isEmpty() || f[1].isEmpty()) {
                problem(i + 1, line, "expected dni;email");
                continue;
            }
            users.add(new User(f[0], f[1]));
        }
        return users;
    }
}
