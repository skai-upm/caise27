package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.exception.PersistenceException;

import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads and writes users in a text file, one user per line with the
 * attributes separated by ';'.
 */
public class UserFileStorage {

    private static final int FIELDS = 7;

    private final Path file;
    private int skippedLines;

    public UserFileStorage(Path file) {
        this.file = file;
    }

    /**
     * Loads the stored users. A missing file means there are no users yet;
     * lines that do not have the expected fields are skipped.
     */
    public List<User> load() {
        List<User> users = new ArrayList<>();
        skippedLines = 0;
        if (!Files.exists(file)) {
            return users;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(";", -1);
                if (f.length != FIELDS) {
                    skippedLines++;
                    continue;
                }
                users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not read users file " + file, e);
        }
        return users;
    }

    public void save(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(";", u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getPhone(), u.getCard()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Could not write users file " + file, e);
        }
    }

    public int getSkippedLines() {
        return skippedLines;
    }
}
