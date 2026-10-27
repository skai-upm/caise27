package es.upm.amusementpark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads and writes groups, one per line: {@code id;name;dni1;dni2;...}.
 * Members are linked to the active users by DNI.
 */
public class GroupFileStorage {

    private final Path file;
    private int skippedLines;

    public GroupFileStorage(Path file) {
        this.file = file;
    }

    public List<Group> load(UserRepository users) {
        List<Group> groups = new ArrayList<>();
        skippedLines = 0;
        for (String[] f : TextFiles.readRecords(file)) {
            if (f.length < 2) {
                skippedLines++;
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (int i = 2; i < f.length; i++) {
                users.findByDni(f[i]).ifPresent(group::addMember);
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            StringBuilder line = new StringBuilder(group.getIdentifier()).append(';').append(group.getName());
            for (User member : group.getMembers()) {
                line.append(';').append(member.getDni());
            }
            lines.add(line.toString());
        }
        TextFiles.writeLines(file, lines);
    }

    public int getSkippedLines() {
        return skippedLines;
    }
}
