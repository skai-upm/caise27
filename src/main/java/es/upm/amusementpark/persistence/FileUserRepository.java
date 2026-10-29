package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.repository.UserRepository;
import es.upm.amusementpark.business.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line with attributes separated by ';'.
 * The password field holds the BCrypt hash, never the plain password.
 */
public class FileUserRepository implements UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path path;
    private final PrintStream warnings;

    public FileUserRepository(Path path, PrintStream warnings) {
        this.path = path;
        this.warnings = warnings;
    }

    /**
     * Loads all users from the file. A missing file yields an empty list; malformed lines are skipped.
     */
    @Override
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(path)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                if (f.length != FIELDS) {
                    warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + path);
                    continue;
                }
                users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
            }
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    @Override
    public void saveAll(Collection<User> users) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (User u : users) {
                writer.write(String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(),
                        u.getEmail(), u.getPasswordHash(), u.getPhone(), u.getCard()));
                writer.newLine();
            }
        }
    }
}
