package es.upm.amusementpark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Persists groups as {@code GROUP;id;name} lines followed by one
 * {@code MEMBER;groupId;email} line per member.
 */
public class GroupRepository extends TextFileRepository {

    public GroupRepository(Path file) {
        super(file);
    }

    public void saveAll(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            lines.add(join("GROUP", group.getIdentifier(), group.getName()));
            for (User member : group.getMembers()) {
                lines.add(join("MEMBER", group.getIdentifier(), member.getEmail()));
            }
        }
        writeLines(lines);
    }

    public void loadInto(ParkController park, List<String> skipped) {
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                if (f.length == 3 && f[0].equals("GROUP")) {
                    park.restoreGroup(f[1], f[2]);
                } else if (f.length == 3 && f[0].equals("MEMBER")) {
                    park.restoreMember(f[1], f[2]);
                } else {
                    skipped.add(line);
                }
            } catch (IllegalArgumentException e) {
                skipped.add(line);
            }
        }
    }
}
