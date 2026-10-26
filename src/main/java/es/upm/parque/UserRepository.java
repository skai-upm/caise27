package es.upm.parque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file: one user per line, attributes separated by ';'.
 */
public class UserRepository {
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                if (f.length != FIELDS) {
                    System.err.println("Ignoring malformed user line: " + line);
                    continue;
                }
                users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read users file " + file + ": " + e.getMessage(), e);
        }
        return users;
    }

    public void save(List<User> users) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (User u : users) {
                    writer.write(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(), u.getEmail(),
                            u.getPassword(), u.getPhone(), u.getCard()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write users file " + file + ": " + e.getMessage(), e);
        }
    }
}
