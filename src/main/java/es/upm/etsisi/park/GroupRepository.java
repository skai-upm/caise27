package es.upm.etsisi.park;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are referenced by their
 * immutable DNI and resolved to user objects when loading.
 */
public class GroupRepository extends TextFileRepository {

    private static final int FIELDS = 3;
    private static final String MEMBER_SEPARATOR = ",";

    public GroupRepository(Path file) {
        super(file);
    }

    public List<Group> load(Map<String, User> usersByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        for (String line : readLines()) {
            String[] f = fields(line, FIELDS);
            if (f.length == 0) {
                warnMalformed(line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User member = usersByDni.get(dni);
                    if (member != null && !group.contains(member)) {
                        group.add(member);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void saveAll(Collection<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = g.getMembers().stream().map(User::getDni).toList();
            lines.add(String.join(SEPARATOR, g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        writeLines(lines);
    }
}
