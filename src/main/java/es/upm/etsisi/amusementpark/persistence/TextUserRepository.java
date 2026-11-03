package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.repository.UserRepository;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores users in a text file, one user per line with ';'-separated attributes:
 * FirstName;Surnames;DNI;Email;Password;Phone;Card
 * where Password holds the protected (hashed) value, never the original password.
 */
public class TextUserRepository implements es.upm.etsisi.amusementpark.business.repository.UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public TextUserRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /**
     * Loads all stored users. A missing file means no users yet; lines that do not
     * describe a valid, unique user are skipped with a warning.
     */
    @Override
    public List<User> load() throws StorageException {
        List<User> users = new ArrayList<>();
        Set<String> dnis = new HashSet<>();
        Set<String> emails = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFile.readLines(file)) {
            lineNumber++;
            if (!line.isBlank()) {
                User user = parse(line);
                if (user != null && !dnis.contains(user.getDni()) && !emails.contains(user.getEmail())) {
                    dnis.add(user.getDni());
                    emails.add(user.getEmail());
                    users.add(user);
                } else {
                    warnings.println("WARNING: skipping invalid user record at line " + lineNumber + " of " + file);
                }
            }
        }
        return users;
    }

    /** Replaces the file contents with the given users. */
    @Override
    public void save(List<User> users) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(format(user));
        }
        TextFile.writeLines(file, lines);
    }

    static String format(User user) {
        return String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                user.getEmail(), user.getPasswordHash(), user.getPhone(), user.getCard());
    }

    static User parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            return null;
        }
        User user = new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
        boolean valid = UserValidator.isValidDni(user.getDni())
                && UserValidator.isValidEmail(user.getEmail())
                && !user.getPasswordHash().isEmpty()
                && UserValidator.isValidCard(user.getCard());
        return valid ? user : null;
    }
}
