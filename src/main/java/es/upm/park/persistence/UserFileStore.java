package es.upm.park.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.park.model.User;

/**
 * Reads and writes users in a text file: one user per line, attributes
 * separated by ';' (FirstName;Surnames;DNI;Email;Password;Phone;Card).
 */
public class UserFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserFileStore(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file yields no users; blank or
     * malformed lines are skipped.
     */
    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(SEPARATOR, -1);
                if (!line.isBlank() && f.length == FIELDS) {
                    users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("cannot read " + file, e);
        }
        return users;
    }

    /**
     * Replaces the file content with the given users.
     */
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getTelephone(), u.getCardNumber()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("cannot write " + file, e);
        }
    }
}
