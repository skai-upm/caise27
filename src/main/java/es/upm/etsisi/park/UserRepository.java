package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line, attributes separated by ';'.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet.
     * Lines that do not have exactly seven fields are skipped with a warning.
     */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                if (f.length != FIELDS) {
                    System.err.println("WARNING: skipping malformed user record at line " + lineNumber);
                    continue;
                }
                users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
            }
        }
        return users;
    }

    /**
     * Replaces the stored users with the given ones. Writes to a temporary file
     * first so that a failed write never leaves a half-written data file.
     */
    public void saveAll(Collection<User> users) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp = Files.createTempFile(dir, "users", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                for (User u : users) {
                    writer.write(toLine(u));
                    writer.newLine();
                }
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    static String toLine(User u) {
        return String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(),
                u.getEmail(), u.getPassword(), u.getPhone(), u.getCard());
    }
}
