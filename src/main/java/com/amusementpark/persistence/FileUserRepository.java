package com.amusementpark.persistence;

import com.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with fields separated by ';'.
 */
public class FileUserRepository implements UserRepository {
    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public FileUserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all stored users. A missing file yields an empty list; malformed lines are skipped.
     */
    @Override
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length == FIELD_COUNT) {
                users.add(new User(fields[0], fields[1], fields[2], fields[3],
                        fields[4], fields[5], fields[6]));
            }
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    @Override
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(SEPARATOR, user.getName(), user.getSurname(), user.getDni(),
                    user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
