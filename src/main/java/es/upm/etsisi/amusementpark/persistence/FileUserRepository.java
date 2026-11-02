package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.business.repository.UserRepository;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Stores users in a text file, one user per line, attributes separated by ';'.
 */
public class FileUserRepository implements UserRepository {
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public FileUserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means no users; malformed lines are
     * skipped and reported through {@link #getWarnings()}.
     */
    @Override
    public List<User> load() throws IOException {
        warnings.clear();
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                warnings.add("skipped malformed user line " + (i + 1) + " in " + file);
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
                    u.getPassword(), u.getPhone(), u.getCard()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
