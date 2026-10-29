package es.upm.etsisi.amusementpark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are referenced by their immutable DNI.
 */
public class GroupRepository {

    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads groups, resolving members among the given active users. Unknown members are skipped.
     */
    public List<Group> load(UserService users) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != 3) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User member = users.findByDni(dni);
                    if (member == null) {
                        System.err.println("WARNING: group " + f[0] + " references unknown user " + dni);
                    } else {
                        group.add(member);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : g.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(TextFiles.SEPARATOR, g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
