package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.repository.GroupRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are referenced by their immutable DNI.
 */
public class TextGroupRepository implements GroupRepository {

    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public TextGroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads groups, resolving members among the active users. Unknown members are skipped.
     */
    @Override
    public List<Group> load(Function<String, User> userByDni) {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != 3) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User member = userByDni.apply(dni);
                    if (member == null) {
                        System.err.println("WARNING: group " + f[0] + " references unknown user " + dni);
                    } else {
                        group.add(member);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void save(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : g.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(TextFiles.SEPARATOR, g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
