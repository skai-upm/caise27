package es.upm.etsisi.amusementpark.repository;

import es.upm.etsisi.amusementpark.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes users as ';'-separated lines in a text file.
 */
public class UserFileStore {

    private static final int FIELD_COUNT = 7;

    private final LineFile file;

    public UserFileStore(Path file) {
        this.file = new LineFile(file);
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        for (String[] f : file.readRecords()) {
            if (f.length != FIELD_COUNT) {
                file.warnMalformed(f);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(LineFile.join(u.getFirstName(), u.getSurnames(), u.getDni(),
                    u.getEmail(), u.getPassword(), u.getPhone(), u.getCardNumber()));
        }
        file.writeLines(lines);
    }
}
