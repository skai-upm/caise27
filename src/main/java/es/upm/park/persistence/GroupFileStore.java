package es.upm.park.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;

/**
 * Stores groups as {@code id;name;dni1,dni2,...} lines; members are linked
 * back to their user objects when loaded.
 */
public class GroupFileStore {

    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELDS = 3;

    private final Path file;

    public GroupFileStore(Path file) {
        this.file = file;
    }

    /** Rebuilds the groups; DNIs of users that no longer exist are dropped. */
    public List<Group> load(Function<String, Optional<User>> userByDni) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFile.read(file)) {
            String[] f = TextFile.fields(line);
            if (f.length == FIELDS && !f[0].isEmpty()) {
                Group group = new Group(f[0], f[1]);
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    if (!dni.isEmpty()) {
                        userByDni.apply(dni).ifPresent(group::addMember);
                    }
                }
                groups.add(group);
            }
        }
        return groups;
    }

    public void save(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            lines.add(String.join(TextFile.SEPARATOR, g.getId(), g.getName(),
                    String.join(MEMBER_SEPARATOR, g.getMemberDnis())));
        }
        TextFile.write(file, lines);
    }
}
