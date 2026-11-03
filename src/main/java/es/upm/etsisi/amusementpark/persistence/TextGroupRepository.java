package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.repository.GroupRepository;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores groups in a text file, one group per line: Identifier;Name[;MemberDni...]
 */
public class TextGroupRepository implements es.upm.etsisi.amusementpark.business.repository.GroupRepository {

    private static final String SEPARATOR = ";";

    private final Path file;
    private final PrintStream warnings;

    public TextGroupRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /** Loads groups, linking members to the given users by DNI. */
    @Override
    public List<Group> load(Map<String, User> usersByDni) throws StorageException {
        List<Group> groups = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFile.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length < 2 || fields[0].isBlank() || !identifiers.add(fields[0])) {
                warnings.println("WARNING: skipping invalid group record at line " + lineNumber + " of " + file);
            } else {
                groups.add(parseGroup(fields, usersByDni, lineNumber));
            }
        }
        return groups;
    }

    private Group parseGroup(String[] fields, Map<String, User> usersByDni, int lineNumber) {
        Group group = new Group(fields[0], fields[1]);
        for (int i = 2; i < fields.length; i++) {
            User member = usersByDni.get(fields[i]);
            if (member == null || group.hasMember(member)) {
                warnings.println("WARNING: skipping unknown member " + fields[i] + " at line " + lineNumber + " of " + file);
            } else {
                group.addMember(member);
            }
        }
        return group;
    }

    @Override
    public void save(List<Group> groups) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            StringBuilder line = new StringBuilder(group.getIdentifier()).append(SEPARATOR).append(group.getName());
            for (User member : group.getMembers()) {
                line.append(SEPARATOR).append(member.getDni());
            }
            lines.add(line.toString());
        }
        TextFile.writeLines(file, lines);
    }
}
