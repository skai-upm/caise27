package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import es.upm.parque.model.User;

/**
 * Reads and writes users in a text file: one user per line, attributes separated by ';'
 * in the order first name, surnames, DNI, email, password, telephone, card number.
 */
public class UserFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;
    private int malformedLineCount;

    public UserFileStore(Path file) {
        this.file = file;
    }

    /** Number of non-blank lines skipped by the last {@link #load()} because they lacked 7 fields. */
    public int getMalformedLineCount() {
        return malformedLineCount;
    }

    /** Loads all well-formed users; a missing file yields an empty list and malformed lines are skipped. */
    public List<User> load() {
        malformedLineCount = 0;
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        List<String[]> rows;
        try {
            rows = Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(SEPARATOR, -1))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read users file " + file, e);
        }
        malformedLineCount = (int) rows.stream().filter(f -> f.length != FIELDS).count();
        return rows.stream()
                .filter(f -> f.length == FIELDS)
                .map(f -> new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** Replaces the file content with the given users, one per line. */
    public void save(List<User> users) {
        List<String> lines = users.stream()
                .map(u -> String.join(SEPARATOR, u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(),
                        u.getPassword(), u.getTelephone(), u.getCardNumber()))
                .toList();
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write users file " + file, e);
        }
    }
}
