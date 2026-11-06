package es.upm.park.persistence;

import es.upm.park.business.model.Group;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/** Stores groups and their members: {@code id;name;email1;email2;...}. */
public class GroupRepository {

    private final Path file;
    private final PrintStream warnings;

    public GroupRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Group> loadAll() {
        return TextFile.parseAll(file, fields -> {
            TextFile.requireFields(fields, 2);
            if (fields[0].isBlank() || fields[1].isBlank()) {
                throw new IllegalArgumentException("Group without identifier or name");
            }
            Group group = new Group(fields[0], fields[1]);
            for (int i = 2; i < fields.length; i++) {
                group.addMember(fields[i]);
            }
            return group;
        }, warnings);
    }

    public void saveAll(List<Group> groups) {
        TextFile.writeLines(file, groups.stream().map(GroupRepository::format).toList());
    }

    private static String format(Group group) {
        StringBuilder line = new StringBuilder(group.getId()).append(TextFile.SEPARATOR).append(group.getName());
        group.getMemberEmails().forEach(email -> line.append(TextFile.SEPARATOR).append(email));
        return line.toString();
    }
}
