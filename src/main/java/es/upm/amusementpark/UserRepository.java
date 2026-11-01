package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line, with attributes separated by ';'
 * in the order: name, surname, DNI, email, password, phone, card.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file means there are no users yet;
     * blank or malformed lines are skipped.
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
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length != FIELD_COUNT) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                    continue;
                }
                users.add(new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
            }
        }
        return users;
    }

    /** Rewrites the file so that it contains exactly the given users. */
    public void save(List<User> users) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (User user : users) {
                writer.write(toLine(user));
                writer.newLine();
            }
        }
    }

    private static String toLine(User user) {
        return String.join(SEPARATOR, user.getName(), user.getSurname(), user.getDni(),
                user.getEmail(), user.getPassword(), user.getPhone(), user.getCard());
    }
}
