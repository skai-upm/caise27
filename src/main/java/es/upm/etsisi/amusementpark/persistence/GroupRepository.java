package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores groups and their members, one group per line: {@code id;name;email1;email2;...}.
 */
public class GroupRepository extends TextFileRepository {

    public GroupRepository(Path file) {
        super(file);
    }

    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            StringBuilder line = new StringBuilder(join(group.getIdentifier(), group.getName()));
            for (User member : group.getMembers()) {
                line.append(SEPARATOR).append(member.getEmail());
            }
            lines.add(line.toString());
        }
        writeLines(lines);
    }

    /**
     * Loads groups, resolving members by e-mail among the active users.
     */
    public List<Group> load(Function<String, Optional<User>> usersByEmail) {
        List<Group> groups = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f.length < 2 || f[0].isEmpty() || f[1].isEmpty()) {
                problem(i + 1, line, "expected id;name;members...");
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (int m = 2; m < f.length; m++) {
                Optional<User> member = usersByEmail.apply(f[m]);
                if (member.isPresent() && !group.hasMember(f[m])) {
                    group.addMember(member.get());
                } else {
                    problem(i + 1, line, "unknown or repeated member " + f[m]);
                }
            }
            groups.add(group);
        }
        return groups;
    }
}
