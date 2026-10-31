package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are written by DNI
 * and resolved back to the registered users when loading.
 */
public class GroupRepository {

    private static final int FIELD_COUNT = 3;
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    public List<Group> loadAll(Function<String, User> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        for (String[] fields : TextFileStore.readRecords(file, FIELD_COUNT)) {
            Group group = new Group(fields[0], fields[1]);
            if (!fields[2].isEmpty()) {
                for (String dni : fields[2].split(MEMBER_SEPARATOR)) {
                    User user = userByDni.apply(dni);
                    if (user == null) {
                        System.err.println("WARNING: group " + group.getId() + " references unknown DNI " + dni);
                    } else {
                        group.addMember(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void saveAll(Collection<Group> groups) throws IOException {
        List<String[]> records = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            records.add(new String[] {group.getId(), group.getName(), String.join(MEMBER_SEPARATOR, dnis)});
        }
        TextFileStore.writeRecords(file, records);
    }
}
