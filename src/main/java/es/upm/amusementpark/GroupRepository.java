package es.upm.amusementpark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}.
 */
public class GroupRepository {

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    public Map<String, Group> load(Map<String, User> usersByDni) {
        Map<String, Group> groups = new LinkedHashMap<>();
        for (String[] f : TextFiles.readRecords(file)) {
            if (f.length != 3) {
                TextFiles.warnMalformed(file);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(",")) {
                    User user = usersByDni.get(dni);
                    if (user != null) {
                        group.add(user);
                    }
                }
            }
            groups.put(group.getId(), group);
        }
        return groups;
    }

    public void save(Iterable<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User u : g.getMembers()) {
                dnis.add(u.getDni());
            }
            lines.add(String.join(TextFiles.SEPARATOR, g.getId(), g.getName(), String.join(",", dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
