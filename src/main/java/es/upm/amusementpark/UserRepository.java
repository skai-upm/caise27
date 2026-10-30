package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and retrieves users. Users are kept in memory and written through to a text file,
 * one user per line with attributes separated by ';'.
 */
public class UserRepository {

    public static final String DEFAULT_FILE = "users.txt";

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final List<User> users = new ArrayList<>();

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads users from the file. A missing file means there are no users yet.
     */
    public void load() {
        users.clear();
        if (!Files.exists(file)) {
            return;
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
                if (fields.length != FIELD_COUNT) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                    continue;
                }
                users.add(new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read users file " + file, e);
        }
    }

    /**
     * Writes every user to the file, replacing its previous contents.
     */
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (User user : users) {
                writer.write(toLine(user));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write users file " + file, e);
        }
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Whether the given user object is a currently registered user.
     */
    public boolean contains(User user) {
        return users.contains(user);
    }

    public void add(User user) {
        users.add(user);
        save();
    }

    public void remove(User user) {
        users.remove(user);
        save();
    }

    /**
     * Persists changes made to a user already held by this repository.
     */
    public void update(User user) {
        save();
    }

    private static String toLine(User user) {
        return String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(), user.getEmail(),
                user.getPassword(), user.getPhone(), user.getCard());
    }
}
