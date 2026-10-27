package es.upm.amusementpark.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Stores groups as {@code id;name;dni,dni,...}; members are resolved against the loaded users.
 */
public class GroupFileStore {

    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupFileStore(Path file) {
        this.file = file;
    }

    public List<Group> load(UserRepository users) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.read(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != 3) {
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User user = users.findByDni(dni);
                    if (user != null) {
                        group.addMember(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : g.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(TextFiles.join(g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.write(file, lines);
    }
}
