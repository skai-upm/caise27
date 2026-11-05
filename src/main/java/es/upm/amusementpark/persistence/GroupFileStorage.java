package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.UserRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores groups in a text file, one per line: identifier;name;dni1,dni2,...
 */
public class GroupFileStorage implements GroupStorage {

    private static final String MEMBER_SEPARATOR = ",";
    private final Path file;

    public GroupFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Group> load(UserRepository users) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != 3) {
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR, -1)) {
                    users.findByDni(dni).ifPresent(group::add);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = g.getMembers().stream().map(User::getDni).toList();
            lines.add(String.join(TextFiles.SEPARATOR, g.getIdentifier(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
