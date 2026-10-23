package es.upm.park.persistence.file;

import es.upm.park.business.model.User;
import es.upm.park.persistence.UserRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with attributes separated
 * by ';' in the order: first name, surnames, DNI, email, password hash, phone,
 * card.
 */
public class UserFileRepository implements UserRepository {

    private static final int FIELD_COUNT = 7;

    private final Path file;

    public UserFileRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /**
     * Loads all users from the file. A missing file yields an empty list;
     * malformed lines are skipped and reported on standard error.
     */
    @Override
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            User user = parse(line);
            if (user == null) {
                TextFiles.warnMalformed(file, line);
            } else {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Replaces the file contents with the given users.
     */
    @Override
    public void saveAll(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(format(user));
        }
        TextFiles.writeLines(file, lines);
    }

    static String format(User user) {
        return String.join(TextFiles.SEPARATOR,
                user.getFirstName(),
                user.getSurnames(),
                user.getDni(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPhone(),
                user.getCard());
    }

    static User parse(String line) {
        String[] fields = TextFiles.split(line);
        if (fields.length != FIELD_COUNT) {
            return null;
        }
        for (String field : fields) {
            if (field.isEmpty()) {
                return null;
            }
        }
        return new User(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
    }
}
