package es.upm.amusementpark.persistence.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}; members are resolved back to users on load.
 */
public class GroupFileStorage {

    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupFileStorage(Path file) {
        this.file = file;
    }

    public List<Group> load(Function<String, Optional<User>> userByDni, List<String> warnings) throws IOException {
        List<Group> groups = new ArrayList<>();
        int n = 0;
        for (String line : TextFiles.readLines(file)) {
            n++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            if (f.length != 3) {
                warnings.add("Skipping malformed group at line " + n + " of " + file);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    Optional<User> user = userByDni.apply(dni);
                    if (user.isPresent()) {
                        group.addMember(user.get());
                    } else {
                        warnings.add("Removed unknown user " + dni + " from group " + group.getId());
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            lines.add(String.join(TextFiles.SEPARATOR, g.getId(), g.getName(),
                    String.join(MEMBER_SEPARATOR, g.getMembers().stream().map(User::getDni).toList())));
        }
        TextFiles.writeLines(file, lines);
    }
}
