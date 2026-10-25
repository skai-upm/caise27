package es.upm.parque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
public class UserRepository {

    public static final Path DEFAULT_PATH = Path.of("data", "users.txt");

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public UserRepository() {
        this(DEFAULT_PATH);
    }

    /**
     * Loads all stored users. A missing file means there are no users yet.
     * Malformed lines are skipped with a warning.
     */
    public List<User> load() {
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
                String[] fields = line.split(SEPARATOR, -1);
                if (fields.length != FIELD_COUNT || !UserValidator.isValidDni(fields[2])) {
                    System.err.println("WARNING: skipping malformed user at " + file + ":" + lineNumber);
                    continue;
                }
                users.add(new User(fields[0], fields[1], fields[2], fields[3],
                        fields[4], fields[5], fields[6]));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read users file " + file, e);
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    public void saveAll(Collection<User> users) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (User user : users) {
                    writer.write(toLine(user));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write users file " + file, e);
        }
    }

    private static String toLine(User user) {
        return String.join(SEPARATOR, user.getName(), user.getSurnames(), user.getDni(),
                user.getEmail(), user.getPassword(), user.getPhone(), user.getCard());
    }
}
