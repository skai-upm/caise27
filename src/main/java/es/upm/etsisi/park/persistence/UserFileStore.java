package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes users as ';'-separated lines in a text file.
 */
public class UserFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileStore(Path file) {
        this.file = file;
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read user data file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length != FIELD_COUNT) {
                System.err.println("WARNING: skipping malformed user line in " + file);
                continue;
            }
            users.add(new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]));
        }
        return users;
    }

    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                    user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            Path temp = Files.createTempFile(parent, "users", ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write user data file " + file, e);
        }
    }
}
