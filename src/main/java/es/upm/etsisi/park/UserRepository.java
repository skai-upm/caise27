package es.upm.etsisi.park;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line, attributes separated by ';'.
 */
public class UserRepository {

    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        for (String line : TextFile.readLines(file)) {
            String[] f = TextFile.fields(line);
            if (f.length != FIELDS) {
                TextFile.warnMalformed(file, line);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    public void save(Collection<User> users) {
        TextFile.write(file, toLines(users));
    }

    List<String> toLines(Collection<User> users) {
        return users.stream().map(UserRepository::toLine).toList();
    }

    static String toLine(User u) {
        return TextFile.join(u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                u.getPassword(), u.getPhone(), u.getCard());
    }
}
