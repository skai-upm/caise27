package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.model.User;

/**
 * Stores users in a text file, one user per line with its attributes separated by {@code ;}.
 */
public class UserFileStorage implements UserStorage {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<User> load(List<String> warnings) throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                warnings.add("Skipping malformed user at line " + lineNumber + " of " + file);
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
            lines.add(String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getPhone(), u.getCardNumber()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
