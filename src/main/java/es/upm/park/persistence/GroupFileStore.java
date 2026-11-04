package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line: {@code Id;Nombre;DNI1;DNI2;...}.
 * Members are referenced by DNI and resolved to the registered users on load.
 */
public class GroupFileStore {

    private static final String SEPARATOR = ";";

    private final Path file;

    public GroupFileStore(Path file) {
        this.file = file;
    }

    /** Loads groups, linking members through {@code userByDni}; unknown members are skipped. */
    public List<Group> load(Function<String, Optional<User>> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length < 2 || f[0].isBlank() || f[1].isBlank()) {
                System.err.println("Skipping malformed group record at line " + lineNumber + " of " + file);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (int i = 2; i < f.length; i++) {
                Optional<User> member = userByDni.apply(f[i]);
                if (member.isPresent()) {
                    group.addMember(member.get());
                } else {
                    System.err.println("Skipping unknown member " + f[i] + " of group " + f[0]);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    /** Replaces the file content with the given groups. */
    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            StringBuilder line = new StringBuilder(g.getId()).append(SEPARATOR).append(g.getName());
            for (User u : g.getMembers()) {
                line.append(SEPARATOR).append(u.getDni());
            }
            lines.add(line.toString());
        }
        Path absolute = file.toAbsolutePath();
        Path dir = absolute.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp = Files.createTempFile(dir, absolute.getFileName().toString(), ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
