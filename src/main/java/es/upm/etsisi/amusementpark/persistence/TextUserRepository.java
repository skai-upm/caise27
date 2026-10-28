package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.repository.UserRepository;
import es.upm.etsisi.amusementpark.business.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users, one per line, in the assignment format
 * {@code FirstName;Surnames;DNI;Email;Password;Phone;Card}, where Password is only the
 * BCrypt value (empty when the user has none). Lines of earlier versions
 * ({@code dni;email}, {@code dni;email;hash} and {@code dni;email;hash;firstName;surnames;phone;card})
 * are still read.
 */
public class TextUserRepository extends TextFileRepository implements UserRepository {

    public TextUserRepository(Path file) {
        super(file);
    }

    @Override
    public void save(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(join(user.getFirstName(), user.getSurnames(), user.getDni(), user.getEmail(),
                    user.hasPassword() ? user.getPasswordHash() : "", user.getPhone(), user.getCard()));
        }
        writeLines(lines);
    }

    @Override
    public List<User> load() {
        List<User> users = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f.length == 7 && f[3].contains("@") && !f[2].isEmpty()) {
                users.add(new User(f[0], f[1], f[2], f[3], f[4].isEmpty() ? null : f[4], f[5], f[6]));
            } else if (f.length == 7 && f[1].contains("@") && !f[0].isEmpty()) {
                users.add(new User(f[3], f[4], f[0], f[1], f[2].isEmpty() ? null : f[2], f[5], f[6]));
            } else if ((f.length == 2 || f.length == 3) && !f[0].isEmpty() && f[1].contains("@")) {
                users.add(new User(f[0], f[1], f.length == 3 && !f[2].isEmpty() ? f[2] : null));
            } else {
                problem(i + 1, line, "expected FirstName;Surnames;DNI;Email;Password;Phone;Card");
            }
        }
        return users;
    }
}
