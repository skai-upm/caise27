package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.model.User;

/**
 * Reads and writes users in a text file: one user per line, attributes separated by ';'
 * in the order first name, surnames, DNI, email, password, telephone, card number.
 */
public class UserFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserFileStore(Path file) {
        this.file = file;
    }

    /** Loads all well-formed users; a missing file yields an empty list and malformed lines are skipped. */
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
            throw new UncheckedIOException("cannot read users file " + file, e);
        }
        return users;
    }

    /** Replaces the file content with the given users, one per line. */
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getTelephone(), u.getCardNumber()));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write users file " + file, e);
        }
    }
}
