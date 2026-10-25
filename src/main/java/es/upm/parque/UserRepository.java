package es.upm.parque;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores users in a text file, one user per line with fields separated by ';'.
 */
public class UserRepository {

    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public List<User> load() {
        return TextFile.readRecords(file, FIELDS).stream()
                .map(f -> new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void save(List<User> users) {
        TextFile.writeRecords(file, users.stream()
                .map(u -> TextFile.join(u.getName(), u.getSurname(), u.getDni(),
                        u.getEmail(), u.getPassword(), u.getPhone(), u.getCard()))
                .toList());
    }
}
