package es.upm.park.persistence;

import es.upm.park.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with attributes separated
 * by ';' in the order: first name, surnames, DNI, email, password, phone, card.
 */
public class UserFileRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /**
     * Loads all users from the file. A missing file yields an empty list;
     * malformed lines are skipped and reported on standard error.
     */
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                User user = parse(line);
                if (user == null) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                } else {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read users file " + file, e);
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    public void saveAll(List<User> users) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (User user : users) {
                    writer.write(format(user));
                    writer.newLine();
                }
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write users file " + file, e);
        }
    }

    static String format(User user) {
        return String.join(SEPARATOR,
                user.getFirstName(),
                user.getSurnames(),
                user.getDni(),
                user.getEmail(),
                user.getPassword(),
                user.getPhone(),
                user.getCard());
    }

    static User parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            return null;
        }
        for (String field : fields) {
            if (field.isEmpty()) {
                return null;
            }
        }
        return new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
    }
}
