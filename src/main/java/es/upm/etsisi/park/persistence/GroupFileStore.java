package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are referenced by DNI because it
 * never changes.
 */
public class GroupFileStore {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELD_COUNT = 3;

    private final Path file;

    public GroupFileStore(Path file) {
        this.file = file;
    }

    public List<Group> load(Function<String, Optional<User>> userByDni) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length != FIELD_COUNT) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            Group group = new Group(fields[0], fields[1]);
            if (!fields[2].isEmpty()) {
                for (String dni : fields[2].split(MEMBER_SEPARATOR)) {
                    userByDni.apply(dni).ifPresent(group::add);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(SEPARATOR, group.getId(), group.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
