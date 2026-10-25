package es.upm.parque;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with its attributes separated by ';', and loads them back.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads every stored user. A missing file means there are no users; malformed lines are skipped.
     */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                System.err.println("WARNING Skipping malformed line " + lineNumber + " in " + file);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /**
     * Replaces the file content with the given users.
     */
    public void saveAll(List<User> users) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, "users", ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            for (User user : users) {
                writer.write(toLine(user));
                writer.newLine();
            }
        }
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String toLine(User user) {
        return String.join(SEPARATOR, user.getName(), user.getSurname(), user.getDni(), user.getEmail(),
                user.getPassword(), user.getPhone(), user.getCard());
    }
}
