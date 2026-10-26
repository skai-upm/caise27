package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line: {@code id;name;dni1,dni2,...}.
 * Members are linked to users by DNI because it never changes.
 */
public class GroupRepository {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all groups, resolving each member DNI with {@code userByDni}. Unknown DNIs and malformed lines are skipped.
     */
    public List<Group> loadAll(Function<String, User> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(SEPARATOR, -1);
                if (fields.length == 3) {
                    Group group = new Group(fields[0], fields[1]);
                    for (String dni : fields[2].split(MEMBER_SEPARATOR)) {
                        User user = userByDni.apply(dni);
                        if (user != null) {
                            group.addMember(user);
                        }
                    }
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    public void saveAll(Collection<Group> groups) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Group group : groups) {
                List<String> dnis = group.getMembers().stream().map(User::getDni).toList();
                writer.write(group.getId() + SEPARATOR + group.getName() + SEPARATOR
                        + String.join(MEMBER_SEPARATOR, dnis));
                writer.newLine();
            }
        }
    }
}
