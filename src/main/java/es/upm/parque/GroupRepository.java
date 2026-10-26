package es.upm.parque;

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
        for (String line : TextFiles.readLines(file)) {
            String[] f = line.split(";", -1);
            if (f.length == 3) {
                groups.add(parse(f, usersByDni));
            }
        }
        return groups;
    }

    public void saveAll(List<Group> groups) {
        TextFiles.writeLines(file, groups.stream()
                .map(g -> g.getId() + ";" + g.getName() + ";"
                        + String.join(",", g.getMembers().stream().map(User::getDni).toList()))
                .toList());
    }

    private static Group parse(String[] f, Map<String, User> usersByDni) {
        Group group = new Group(f[0], f[1]);
        for (String dni : f[2].split(",")) {
            User user = usersByDni.get(dni);
            if (user != null && !group.hasMember(user)) {
                group.addMember(user);
            }
        }
        return group;
    }
}
