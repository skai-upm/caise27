package es.upm.parque.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

/**
 * Stores groups in a text file: one group per line as id;name;dni,dni,...
 */
public class GroupRepository {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELDS = 3;

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads the stored groups, linking each member DNI to an existing user through the given lookup.
     */
    public List<Group> load(Function<String, User> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(SEPARATOR, -1);
            if (parts.length != FIELDS || parts[0].isBlank()) {
                continue;
            }
            Group group = new Group(parts[0], parts[1]);
            for (String dni : parts[2].split(MEMBER_SEPARATOR)) {
                User user = userByDni.apply(dni);
                if (user != null) {
                    group.addMember(user);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(Collection<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(SEPARATOR, group.getId(), group.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
