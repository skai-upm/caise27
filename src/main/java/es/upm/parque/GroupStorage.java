package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line: {@code Id;Nombre;DNI1,DNI2,...}.
 * Members are saved by DNI and resolved back to the registered users when loading.
 */
public class GroupStorage {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupStorage(Path file) {
        this.file = file;
    }

    /**
     * @param userByDni resolves a stored DNI to the registered user, or null if it no longer exists
     */
    public List<Group> load(Function<String, User> userByDni) {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(SEPARATOR, -1);
                if (f.length != 3 || f[0].isBlank()) {
                    continue;
                }
                Group group = new Group(f[0], f[1]);
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User user = dni.isEmpty() ? null : userByDni.apply(dni);
                    if (user != null) {
                        group.addMember(user);
                    }
                }
                groups.add(group);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read group file " + file, e);
        }
        return groups;
    }

    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User u : g.getMembers()) {
                dnis.add(u.getDni());
            }
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
