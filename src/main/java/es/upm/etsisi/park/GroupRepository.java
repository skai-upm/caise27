package es.upm.etsisi.park;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores groups as {@code id;name;dni;dni;...}; members are referenced by their immutable DNI.
 */
public class GroupRepository {

    private static final int FIXED_FIELDS = 2;

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public List<Group> load(Map<String, User> usersByDni) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFile.readLines(file)) {
            String[] f = TextFile.fields(line);
            if (f.length < FIXED_FIELDS) {
                TextFile.warnMalformed(file, line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (int i = FIXED_FIELDS; i < f.length; i++) {
                User member = usersByDni.get(f[i]);
                if (member == null) {
                    TextFile.warnMalformed(file, line);
                } else {
                    group.add(member);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    List<String> toLines(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            StringBuilder line = new StringBuilder(TextFile.join(g.getId(), g.getName()));
            for (User member : g.getMembers()) {
                line.append(TextFile.SEPARATOR).append(member.getDni());
            }
            lines.add(line.toString());
        }
        return lines;
    }
}
