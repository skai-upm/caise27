package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line, attributes separated by {@code ;}
 * in the order: name;surname;dni;email;password;phone;card.
 */
public class UserFileRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileRepository(Path file) {
        this.file = file;
    }

    /** Loads all users; a missing file means there are no users yet. */
    public List<User> load() throws IOException {
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
            if (f.length != FIELD_COUNT) {
                System.err.println("Skipping malformed user record at line " + lineNumber + " of " + file);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /** Replaces the file content with the given users. */
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>(users.size());
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getPhone(), u.getCard()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
