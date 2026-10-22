package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes users as ';'-separated lines in a text file.
 */
public class UserFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileStore(Path file) {
        this.file = file;
    }

    public List<User> load() {
        List<User> users = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length != FIELD_COUNT) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            users.add(new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]));
        }
        return users;
    }

    public void save(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join(SEPARATOR, user.getFirstName(), user.getSurnames(), user.getDni(),
                    user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()));
        }
        TextFiles.writeLines(file, lines);
    }
}
