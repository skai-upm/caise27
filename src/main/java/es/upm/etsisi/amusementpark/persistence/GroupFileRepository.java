package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.user.User;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Text-file group storage: {@code id;name;dni1,dni2,...} per line. Members are referenced by DNI.
 */
public class GroupFileRepository {

    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public GroupFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Group> load(Map<String, User> usersByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            if (f.length != FIELD_COUNT) {
                warnings.println("WARNING: ignoring malformed group line in " + file);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (String dni : f[2].isEmpty() ? new String[0] : f[2].split(MEMBER_SEPARATOR)) {
                User member = usersByDni.get(dni);
                if (member == null) {
                    warnings.println("WARNING: unknown member " + dni + " in group " + f[0]);
                } else {
                    group.addMember(member);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        TextFiles.writeLines(file, groups.stream().map(g -> String.join(TextFiles.SEPARATOR, g.getId(), g.getName(),
                String.join(MEMBER_SEPARATOR, g.getMembers().stream().map(User::getDni).toList()))).toList());
    }
}
