package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.UserRepository;

/**
 * Saves users to and loads users from a semicolon-separated text file,
 * one user per line.
 */
public class UserFileRepository implements UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet;
     * blank or malformed lines are skipped.
     */
    @Override
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELD_COUNT) {
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    @Override
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getPhone(), u.getCard()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
