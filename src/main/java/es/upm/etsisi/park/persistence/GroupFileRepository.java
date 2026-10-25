package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.User;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are referenced by their immutable DNI.
 */
public class GroupFileRepository implements GroupRepository {

    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public GroupFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Group> loadAll(Function<String, Optional<User>> userByDni) {
        List<Group> groups = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = TextFiles.fields(line);
            if (parts.length != FIELD_COUNT) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                continue;
            }
            Group group = new Group(parts[0], parts[1]);
            if (!parts[2].isEmpty()) {
                for (String dni : parts[2].split(MEMBER_SEPARATOR)) {
                    Optional<User> member = userByDni.apply(dni);
                    if (member.isPresent()) {
                        group.add(member.get());
                    } else {
                        warnings.println("WARNING: unknown member " + dni + " in group " + group.getId());
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void saveAll(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(TextFiles.SEPARATOR, group.getId(), group.getName(),
                    String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.writeLines(file, lines);
    }
}
