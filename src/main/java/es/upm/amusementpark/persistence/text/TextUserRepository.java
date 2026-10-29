package es.upm.amusementpark.persistence.text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.UserRepository;

/**
 * Stores users in a text file, one per line:
 * {@code firstName;surnames;dni;email;passwordHash;phone;card}.
 */
public class TextUserRepository implements UserRepository {

    private static final int FIELD_COUNT = 7;

    private final Path file;

    public TextUserRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<User> load() {
        List<User> users = new ArrayList<>();
        for (String[] f : TextFiles.readRecords(file)) {
            if (f.length != FIELD_COUNT) {
                TextFiles.warnMalformed(file);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    @Override
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(TextFiles.join(u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                    u.getPasswordHash(), u.getPhone(), u.getCard()));
        }
        TextFiles.writeLines(file, lines);
    }
}
