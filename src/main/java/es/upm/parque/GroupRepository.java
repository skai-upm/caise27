package es.upm.parque;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups as {@code id;name;dni1,dni2,...} lines and restores them linked to the loaded users.
 */
public class GroupRepository {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";

    private final TextFileStore store;

    public GroupRepository(Path file) {
        this.store = new TextFileStore(file);
    }

    public List<Group> load(Function<String, User> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        for (String line : store.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 3) {
                store.warnSkipped(line, "expected 3 fields");
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User user = userByDni.apply(dni);
                    if (user == null) {
                        store.warnSkipped(line, "unknown member " + dni);
                    } else {
                        group.addMember(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void saveAll(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(SEPARATOR, group.getId(), group.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        store.writeLines(lines);
    }
}
