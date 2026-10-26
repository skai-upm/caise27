package es.upm.parque.persistence;

import es.upm.parque.business.model.User;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line with fields separated by ';'.
 */
public class UserStorage {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserStorage(Path file) {
        this.file = file;
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(SEPARATOR, -1);
                if (f.length == FIELD_COUNT) {
                    users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read user file " + file, e);
        }
        return users;
    }

    public void save(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(), u.getEmail(),
                u.getPassword(), u.getPhone(), u.getCard()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write user file " + file, e);
        }
    }
}
