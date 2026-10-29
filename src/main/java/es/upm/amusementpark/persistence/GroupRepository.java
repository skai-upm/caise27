package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}; members are resolved against active users by DNI.
 */
public class GroupRepository {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELDS = 3;

    private final Path path;
    private final PrintStream warnings;

    public GroupRepository(Path path, PrintStream warnings) {
        this.path = path;
        this.warnings = warnings;
    }

    public List<Group> load(Map<String, User> usersByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(path)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + path);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User user = usersByDni.get(dni);
                    if (user == null) {
                        warnings.println("WARNING: unknown member " + dni + " of group " + f[0] + " ignored");
                    } else if (!group.hasMember(user)) {
                        group.addMember(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void saveAll(Collection<Group> groups) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Group group : groups) {
                List<String> dnis = new ArrayList<>();
                for (User member : group.getMembers()) {
                    dnis.add(member.getDni());
                }
                writer.write(String.join(SEPARATOR, group.getId(), group.getName(),
                        String.join(MEMBER_SEPARATOR, dnis)));
                writer.newLine();
            }
        }
    }
}
