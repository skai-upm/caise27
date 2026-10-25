package es.upm.parque;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups as lines {@code id;name;dni1;dni2;...}.
 */
public class GroupRepository {

    public static final String FILE_NAME = "groups.txt";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all groups, linking members through the given DNI lookup. Unknown DNIs are skipped.
     */
    public List<Group> load(Function<String, User> usersByDni) {
        List<Group> groups = new ArrayList<>();
        for (String[] fields : TextStore.readRecords(file)) {
            if (fields.length < 2) {
                TextStore.warnMalformed(file, fields);
                continue;
            }
            Group group = new Group(fields[0], fields[1]);
            for (int i = 2; i < fields.length; i++) {
                User member = usersByDni.apply(fields[i]);
                if (member != null && !group.hasMember(member)) {
                    group.addMember(member);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void saveAll(Collection<Group> groups) {
        TextStore.writeLines(file, groups.stream().map(GroupRepository::toLine).toList());
    }

    private static String toLine(Group group) {
        List<String> fields = new ArrayList<>(List.of(group.getId(), group.getName()));
        group.getMembers().forEach(member -> fields.add(member.getDni()));
        return String.join(TextStore.SEPARATOR, fields);
    }
}
