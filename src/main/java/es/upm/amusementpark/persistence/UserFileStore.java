package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.model.User;

/**
 * Reads and writes users in a text file: one user per line, attributes separated by ';'
 * in the order FirstName;Surnames;DNI;Email;Password;Phone;Card.
 */
public class UserFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserFileStore(Path file) {
        this.file = file;
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(SEPARATOR, -1);
                if (f.length == FIELDS) {
                    users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not read " + file, e);
        }
        return users;
    }

    public void save(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getPhone(), u.getCardNumber()));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Could not write " + file, e);
        }
    }
}
