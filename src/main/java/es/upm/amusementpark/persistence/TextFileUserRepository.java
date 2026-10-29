package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.UserRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Persists users as {@code email;dni;passwordHash;firstName;surnames;phone;card}
 * lines; only the BCrypt-protected password is stored. Older lines with two or
 * three fields are still read.
 */
public class TextFileUserRepository extends TextFileRepository
        implements UserRepository {

    public TextFileUserRepository(Path file) {
        super(file);
    }

    @Override
    public void saveAll(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            String hash = user.getPasswordHash();
            lines.add(join(user.getEmail(), user.getDni(), hash == null ? "" : hash, user.getFirstName(),
                    user.getSurnames(), user.getPhone(), user.getCard()));
        }
        writeLines(lines);
    }

    @Override
    public List<User> loadAll(List<String> skipped) {
        List<User> users = new ArrayList<>();
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f.length == 7 && !f[0].isEmpty()) {
                users.add(new User(f[3], f[4], f[1], f[0], f[2].isEmpty() ? null : f[2], f[5], f[6]));
            } else if ((f.length == 2 || f.length == 3) && !f[0].isEmpty()) {
                User user = new User(f[0], f[1]);
                if (f.length == 3 && !f[2].isEmpty()) {
                    user.setPasswordHash(f[2]);
                }
                users.add(user);
            } else {
                skipped.add(line);
            }
        }
        return users;
    }
}
