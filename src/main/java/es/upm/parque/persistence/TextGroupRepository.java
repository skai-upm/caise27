package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.GroupRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups as lines {@code id;name;dni1;dni2;...}.
 */
public class TextGroupRepository implements GroupRepository {

    public static final String FILE_NAME = "groups.txt";

    private final Path file;

    public TextGroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all groups, linking members through the given DNI lookup. Unknown DNIs are skipped.
     */
    @Override
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

    @Override
    public void saveAll(Collection<Group> groups) {
        TextStore.writeLines(file, groups.stream().map(TextGroupRepository::toLine).toList());
    }

    private static String toLine(Group group) {
        List<String> fields = new ArrayList<>(List.of(group.getId(), group.getName()));
        group.getMembers().forEach(member -> fields.add(member.getDni()));
        return String.join(TextStore.SEPARATOR, fields);
    }
}
