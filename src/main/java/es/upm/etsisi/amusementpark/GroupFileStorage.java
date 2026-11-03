package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores groups in a text file, one group per line: {@code Id;Nombre;DNI1,DNI2,...}.
 * Members are stored by DNI only (immutable), so user data is never duplicated; on load the DNIs
 * are linked to the already loaded user objects. This is the only class that touches the group file.
 */
public class GroupFileStorage {

    public static final String DEFAULT_FILE = "groups.txt";
    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELDS = 3;

    private final Path file;

    public GroupFileStorage(Path file) {
        this.file = file;
    }

    /** Loads the stored groups, linking members to the given registered users. */
    public List<Group> load(List<User> users) {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read group file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                System.err.println("Skipping malformed group record: " + line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    users.stream().filter(u -> u.getDni().equals(dni)).findFirst().ifPresentOrElse(group::addMember,
                            () -> System.err.println("Ignoring unknown member " + dni + " of group " + f[0]));
                }
            }
            groups.add(group);
        }
        return groups;
    }

    /** Rewrites the file so that it contains exactly the given groups. */
    public void save(List<Group> groups) {
        List<String> lines = new ArrayList<>(groups.size());
        for (Group g : groups) {
            List<String> dnis = g.getMembers().stream().map(User::getDni).toList();
            lines.add(String.join(SEPARATOR, g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write group file " + file, e);
        }
    }
}
