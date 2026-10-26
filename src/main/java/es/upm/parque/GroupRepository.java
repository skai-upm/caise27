package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores groups in a text file, one group per line: {@code id;name;dni1,dni2,...}.
 * Members are written by DNI because it never changes.
 */
public class GroupRepository {

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads every stored group, linking members to the given already-loaded users by DNI.
     * DNIs with no matching user and malformed lines are skipped.
     */
    public List<Group> loadAll(Map<String, User> usersByDni) {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(";", -1);
                if (f.length != 3) {
                    continue;
                }
                Group group = new Group(f[0], f[1]);
                for (String dni : f[2].split(",")) {
                    User user = usersByDni.get(dni);
                    if (user != null && !group.hasMember(user)) {
                        group.addMember(user);
                    }
                }
                groups.add(group);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read groups file " + file, e);
        }
        return groups;
    }

    public void saveAll(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = g.getMembers().stream().map(User::getDni).toList();
            lines.add(g.getId() + ";" + g.getName() + ";" + String.join(",", dnis));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write groups file " + file, e);
        }
    }
}
