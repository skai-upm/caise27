package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line, attributes separated by ';'.
 */
class UserFileStore {

    private static final int FIELDS = 7;

    private final Path file;

    UserFileStore(Path file) {
        this.file = file;
    }

    Path getFile() {
        return file;
    }

    List<User> load() {
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

    List<String> toLines(Collection<User> users) {
        return users.stream().map(UserFileStore::toLine).toList();
    }

    static String toLine(User u) {
        return TextFile.join(u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                u.getPassword(), u.getPhone(), u.getCard());
    }
}
