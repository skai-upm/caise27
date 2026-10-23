package es.upm.park.persistence;

import es.upm.park.model.Group;
import es.upm.park.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups as {@code id;name;dni1;dni2;...}, members referenced by DNI.
 */
public class GroupFileRepository {

    private final Path file;

    public GroupFileRepository(Path file) {
        this.file = file;
    }

    /**
     * @param userByDni resolves active users; members that no longer exist are dropped
     */
    public List<Group> loadAll(Function<String, User> userByDni) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = TextFiles.split(line);
            if (fields.length < 2 || fields[0].isEmpty() || fields[1].isEmpty()) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            Group group = new Group(fields[0], fields[1]);
            for (int i = 2; i < fields.length; i++) {
                User member = userByDni.apply(fields[i]);
                if (member == null) {
                    System.err.println("WARNING: group " + group.getId() + " references unknown user " + fields[i]);
                } else {
                    group.add(member);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void saveAll(Iterable<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            StringBuilder line = new StringBuilder(group.getId()).append(TextFiles.SEPARATOR).append(group.getName());
            for (User member : group.getMembers()) {
                line.append(TextFiles.SEPARATOR).append(member.getDni());
            }
            lines.add(line.toString());
        }
        TextFiles.writeLines(file, lines);
    }
}
