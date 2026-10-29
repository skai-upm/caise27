package es.upm.amusementpark.persistence.text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}.
 */
public class TextGroupRepository implements GroupRepository {

    private final Path file;

    public TextGroupRepository(Path file) {
        this.file = file;
    }

    @Override
    public Map<String, Group> load(Map<String, User> usersByDni) {
        Map<String, Group> groups = new LinkedHashMap<>();
        for (String[] f : TextFiles.readRecords(file)) {
            if (f.length != 3) {
                TextFiles.warnMalformed(file);
                continue;
            }
            List<User> members = new ArrayList<>();
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(",")) {
                    User user = usersByDni.get(dni);
                    if (user != null) {
                        members.add(user);
                    }
                }
            }
            groups.put(f[0], new Group(f[0], f[1], members));
        }
        return groups;
    }

    @Override
    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User u : g.getMembers()) {
                dnis.add(u.getDni());
            }
            lines.add(TextFiles.join(g.getId(), g.getName(), String.join(",", dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
