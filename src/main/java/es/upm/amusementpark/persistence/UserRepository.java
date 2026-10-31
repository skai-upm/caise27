package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores users in a text file, one user per line, with attributes separated
 * by {@code ;} in the order: name, surname, DNI, email, password, phone, card.
 */
public class UserRepository {

    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /**
     * Loads every stored user. A missing file means there are no users yet.
     * Blank lines are ignored and malformed lines are skipped with a warning.
     */
    public List<User> loadAll() throws IOException {
        List<User> users = new ArrayList<>();
        for (String[] fields : TextFileStore.readRecords(file, FIELD_COUNT)) {
            users.add(new User(fields[0], fields[1], fields[2], fields[3],
                    fields[4], fields[5], fields[6]));
        }
        return users;
    }

    /** Replaces the file content with the given users. */
    public void saveAll(Collection<User> users) throws IOException {
        List<String[]> records = new ArrayList<>();
        for (User user : users) {
            records.add(new String[] {user.getName(), user.getSurname(), user.getDni(),
                    user.getEmail(), user.getPassword(), user.getPhone(), user.getCard()});
        }
        TextFileStore.writeRecords(file, records);
    }
}
