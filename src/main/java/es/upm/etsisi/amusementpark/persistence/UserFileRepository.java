package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.user.User;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Text-file user storage: one user per line, attributes separated by {@code ;}.
 */
public class UserFileRepository implements UserRepository {

    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public UserFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<User> loadAll() throws IOException {
        List<User> users = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] fields = TextFiles.fields(line);
            if (fields.length != FIELD_COUNT) {
                warnings.println("WARNING: ignoring malformed line " + lineNumber + " in " + file);
                continue;
            }
            users.add(new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]));
        }
        return users;
    }

    @Override
    public void saveAll(List<User> users) throws IOException {
        TextFiles.writeLines(file, users.stream().map(UserFileRepository::toLine).toList());
    }

    static String toLine(User user) {
        return String.join(TextFiles.SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                user.getEmail(), user.getPassword(), user.getPhone(), user.getCard());
    }
}
