package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.user.User;

/**
 * Stores users in a plain text file, one user per line with attributes
 * separated by ';' in the order: first name, surnames, DNI, email,
 * password, phone, card.
 */
public class UserRepository {

    public static final String FILE_PROPERTY = "park.users.file";
    public static final String DEFAULT_FILE = "users.txt";

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public UserRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /** Repository at the path given by the {@value #FILE_PROPERTY} system property, or {@value #DEFAULT_FILE}. */
    public static UserRepository fromSystemProperty(PrintStream warnings) {
        return new UserRepository(Path.of(System.getProperty(FILE_PROPERTY, DEFAULT_FILE)), warnings);
    }

    public Path getFile() {
        return file;
    }

    /** Loads all users. A missing file yields an empty list; malformed lines are skipped with a warning. */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length != FIELD_COUNT) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file);
                continue;
            }
            users.add(new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]));
        }
        return users;
    }

    /** Replaces the file contents with the given users, writing through a temporary file. */
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                    user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()));
        }
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
