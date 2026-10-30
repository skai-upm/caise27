package es.upm.etsisi.amusementpark.repository;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are resolved against the user repository.
 */
public class GroupFileStore {

    private static final String MEMBER_SEPARATOR = ",";

    private final LineFile file;
    private final UserRepository users;

    public GroupFileStore(Path file, UserRepository users) {
        this.file = new LineFile(file);
        this.users = users;
    }

    public List<Group> load() {
        List<Group> groups = new ArrayList<>();
        for (String[] f : file.readRecords()) {
            if (f.length != 3) {
                file.warnMalformed(f);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                if (!dni.isEmpty()) {
                    users.findByDni(dni).ifPresent(group::add);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = g.getMembers().stream().map(User::getDni).toList();
            lines.add(LineFile.join(g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        file.writeLines(lines);
    }
}
