package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line:
 * {@code id;name[;memberDni...]}.
 */
public class FileGroupRepository {

    public static final String DEFAULT_FILE_NAME = "groups.txt";

    private static final String SEPARATOR = ";";

    private final Path file;
    private final PrintStream warnings;

    public FileGroupRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    Path getFile() {
        return file;
    }

    /**
     * Loads all groups, resolving members by DNI. Malformed lines, duplicate
     * identifiers and unknown members are skipped with a warning.
     */
    public List<Group> load(Function<String, User> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length < 2 || fields[0].trim().isEmpty() || fields[1].trim().isEmpty()) {
                warn(lineNumber, "malformed group");
                continue;
            }
            String id = fields[0].trim();
            if (!ids.add(id)) {
                warn(lineNumber, "duplicate group " + id);
                continue;
            }
            Group group = new Group(id, fields[1].trim());
            for (int i = 2; i < fields.length; i++) {
                User member = userByDni.apply(fields[i].trim());
                if (member == null || !group.add(member)) {
                    warn(lineNumber, "unknown or repeated member " + fields[i].trim());
                }
            }
            groups.add(group);
        }
        return groups;
    }

    static List<String> toLines(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            StringBuilder line = new StringBuilder(group.getId()).append(SEPARATOR).append(group.getName());
            for (User member : group.getMembers()) {
                line.append(SEPARATOR).append(member.getDni());
            }
            lines.add(line.toString());
        }
        return lines;
    }

    private void warn(int lineNumber, String reason) {
        warnings.println("WARNING: skipping data in line " + lineNumber + " of " + file + ": " + reason);
    }
}
