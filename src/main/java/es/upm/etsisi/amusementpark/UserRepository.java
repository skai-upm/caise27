package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file: one user per line, attributes separated by ';'.
 */
public class UserRepository {

    public static final Path DEFAULT_FILE = Path.of("users.txt");

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet; malformed lines are skipped.
     */
    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read users file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELD_COUNT) {
                System.err.println("WARNING: skipping malformed user line: " + line);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /**
     * Replaces the stored users with the given list.
     */
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getPhone(), u.getCard()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            Path tmp = Files.createTempFile(parent, "users", ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write users file " + file, e);
        }
    }
}
