package es.upm.etsisi.amusementpark;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores users in a text file, one user per line with ';'-separated attributes:
 * FirstName;Surnames;DNI;Email;Password;Phone;Card
 */
public class UserRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;
    private final PrintStream warnings;

    public UserRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /**
     * Loads all stored users. A missing file means no users yet; lines that do not
     * describe a valid, unique user are skipped with a warning.
     */
    public List<User> load() throws ParkException {
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
    public void save(List<User> users) throws ParkException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(format(user));
        }
        TextFile.writeLines(file, lines);
    }

    static String format(User user) {
        return String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                user.getEmail(), user.getPassword(), user.getPhone(), user.getCard());
    }

    static User parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            return null;
        }
        User user = new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
        boolean valid = UserValidator.isValidDni(user.getDni())
                && UserValidator.isValidEmail(user.getEmail())
                && UserValidator.isValidPassword(user.getPassword())
                && UserValidator.isValidCard(user.getCard());
        return valid ? user : null;
    }
}
