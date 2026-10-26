package es.upm.parque.persistence;

import es.upm.parque.business.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with fields separated by ';'.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet; malformed lines are skipped.
     */
    public List<User> loadAll() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(SEPARATOR, -1);
                if (fields.length == FIELD_COUNT) {
                    users.add(new User(fields[0], fields[1], fields[2], fields[3],
                            fields[4], fields[5], fields[6]));
                }
            }
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    public void saveAll(List<User> users) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (User user : users) {
                writer.write(String.join(SEPARATOR, user.getName(), user.getSurname(), user.getDni(),
                        user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()));
                writer.newLine();
            }
        }
    }
}
