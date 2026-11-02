package com.amusementpark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves users to and loads users from a semicolon-separated text file, one user per line.
 */
public class UserFileStorage {

    private static final int FIELD_COUNT = 7;

    private final LineFile file;

    public UserFileStorage(Path file) {
        this.file = new LineFile(file);
    }

    /**
     * Loads all stored users. A missing file yields an empty list; malformed lines are skipped.
     */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        for (String[] f : file.read(FIELD_COUNT)) {
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /** Replaces the file content with the given users. */
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(LineFile.join(u.getName(), u.getSurname(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getPhone(), u.getCard()));
        }
        file.write(lines);
    }
}
