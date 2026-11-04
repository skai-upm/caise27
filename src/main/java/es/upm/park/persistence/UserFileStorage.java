package es.upm.park.persistence;

import es.upm.park.business.model.User;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line, attributes separated by {@code ;}.
 * This is the only class that reads or writes the users file.
 */
public class UserFileStorage {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileStorage(Path file) {
        this.file = file;
    }

    /** Loads all users; a missing file means no users. Malformed lines are skipped. */
    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                User user = parse(line);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read users file " + file, e);
        }
        return users;
    }

    /** Replaces the file contents with the given users. */
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(format(u));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write users file " + file, e);
        }
    }

    static String format(User u) {
        return String.join(SEPARATOR, u.getName(), u.getSurnames(), u.getDni(), u.getEmail(),
                u.getPassword(), u.getPhone(), u.getCard());
    }

    static User parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELD_COUNT) {
            return null;
        }
        return new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
    }
}
