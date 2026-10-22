package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.validation.UserValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Stores users in a text file, one user per line, with the attributes
 * first name, surnames, DNI, email, password, phone and card separated by
 * {@code ;}.
 */
public class FileUserRepository implements UserRepository {

    public static final String DEFAULT_FILE_NAME = "users.txt";

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public FileUserRepository(Path file) {
        this(file, System.err);
    }

    public FileUserRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<User> loadAll() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        Set<String> dnis = new HashSet<>();
        Set<String> emails = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                User user = parse(line);
                if (user == null) {
                    warn(lineNumber, "malformed or invalid user data");
                    continue;
                }
                String dni = user.getDni().toUpperCase(Locale.ROOT);
                String email = user.getEmail().toLowerCase(Locale.ROOT);
                if (dnis.contains(dni) || emails.contains(email)) {
                    warn(lineNumber, "duplicate DNI or email");
                    continue;
                }
                dnis.add(dni);
                emails.add(email);
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public void saveAll(Collection<User> users) throws IOException {
        TextFiles.writeAll(Map.of(file, toLines(users)));
    }

    Path getFile() {
        return file;
    }

    static List<String> toLines(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(format(user));
        }
        return lines;
    }

    static String format(User user) {
        return String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(), user.getEmail(),
                user.getPassword(), user.getPhone(), user.getCard());
    }

    static User parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            return null;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        boolean valid = UserValidator.isNonBlank(fields[0])
                && UserValidator.isNonBlank(fields[1])
                && UserValidator.isValidDni(fields[2])
                && UserValidator.isValidEmail(fields[3])
                && UserValidator.isValidPassword(fields[4])
                && UserValidator.isNonBlank(fields[5])
                && UserValidator.isValidCard(fields[6]);
        if (!valid) {
            return null;
        }
        return new User(fields[0], fields[1], fields[2].toUpperCase(Locale.ROOT), fields[3], fields[4], fields[5],
                fields[6]);
    }

    private void warn(int lineNumber, String reason) {
        warnings.println("WARNING: skipping line " + lineNumber + " of " + file + ": " + reason);
    }
}
