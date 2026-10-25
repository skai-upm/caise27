package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.User;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line with attributes separated by ';':
 * firstName;surnames;dni;email;password;phone;card
 */
public class UserFileRepository implements UserRepository {

    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public UserFileRepository(Path file) {
        this(file, System.err);
    }

    public UserFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = TextFiles.fields(line);
            if (parts.length != FIELD_COUNT) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                continue;
            }
            users.add(new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
        }
        return users;
    }

    @Override
    public void saveAll(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(toLine(user));
        }
        TextFiles.writeLines(file, lines);
    }

    static String toLine(User user) {
        return String.join(TextFiles.SEPARATOR,
                user.getFirstName(),
                user.getSurnames(),
                user.getDni(),
                user.getEmail(),
                user.getPassword(),
                user.getPhone(),
                user.getCard());
    }
}
