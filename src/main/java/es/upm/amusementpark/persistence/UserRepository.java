package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a semicolon-separated text file, one user per line, and loads them back.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELD_COUNT) {
                System.err.println("WARNING: skipping malformed user record: " + line);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getPhone(), u.getCard()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
