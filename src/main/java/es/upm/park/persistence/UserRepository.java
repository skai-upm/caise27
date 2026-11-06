package es.upm.park.persistence;

import es.upm.park.business.controller.UserStorage;
import es.upm.park.business.model.User;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Stores users one per line: {@code FirstName;Surnames;DNI;Email;Password;Phone;Card}.
 * The password field holds the protected value.
 */
public class UserRepository implements UserStorage {

    private static final int FIELDS = 7;

    private final Path file;
    private final PrintStream warnings;

    public UserRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<User> load() {
        return TextFile.parseAll(file, fields -> {
            if (fields.length != FIELDS) {
                throw new IllegalArgumentException("Expected " + FIELDS + " fields");
            }
            return new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
        }, warnings);
    }

    @Override
    public void save(List<User> users) {
        TextFile.writeLines(file, users.stream().map(user -> String.join(TextFile.SEPARATOR,
                user.getFirstName(), user.getSurnames(), user.getDni(), user.getEmail(), user.getPassword(),
                user.getPhone(), user.getCard())).toList());
    }
}
