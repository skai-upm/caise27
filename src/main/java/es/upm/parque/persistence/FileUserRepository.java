package es.upm.parque.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.UserRepository;

/**
 * Stores users in a text file, one user per line with fields separated by ';'
 * (the password field holds its hash).
 */
public class FileUserRepository implements UserRepository {

    private static final int FIELDS = 7;

    private final Path file;

    public FileUserRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<User> load() {
        return TextFile.readRecords(file, FIELDS).stream()
                .map(f -> new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void save(List<User> users) {
        TextFile.writeRecords(file, users.stream()
                .map(u -> TextFile.join(u.getName(), u.getSurname(), u.getDni(),
                        u.getEmail(), u.getPasswordHash(), u.getPhone(), u.getCard()))
                .toList());
    }
}
