package es.upm.parque;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line with its attributes separated by ';'.
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(SEPARATOR, -1);
            if (parts.length == FIELDS) {
                users.add(new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
            }
        }
        return users;
    }

    public void save(Collection<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(SEPARATOR, user.getName(), user.getSurname(), user.getDni(),
                    user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
