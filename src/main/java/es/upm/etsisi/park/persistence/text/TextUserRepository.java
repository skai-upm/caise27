package es.upm.etsisi.park.persistence.text;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.persistence.UserRepository;

/**
 * Stores users in a plain text file, one user per line with attributes
 * separated by ';' in the order: first name, surnames, DNI, email,
 * BCrypt password hash, phone, card.
 */
public class TextUserRepository implements UserRepository {

    public static final String FILE_NAME = "users.txt";

    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public TextUserRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public Path getFile() {
        return file;
    }

    /** Loads all users. A missing file yields an empty list; malformed lines are skipped with a warning. */
    @Override
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        List<String> lines = TextFiles.readLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = TextFiles.fields(line);
            if (fields.length != FIELD_COUNT) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file);
                continue;
            }
            users.add(new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]));
        }
        return users;
    }

    /** Replaces the file contents with the given users. */
    @Override
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(TextFiles.SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                    user.getEmail(), user.getPasswordHash(), user.getPhone(), user.getCard()));
        }
        TextFiles.write(file, lines);
    }
}
