package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.user.User;

/**
 * Stores groups one per line: identifier;name;memberDni;memberDni;...
 */
public class GroupStorage {
    private static final String SEPARATOR = ";";

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public GroupStorage(Path file) {
        this.file = file;
    }

    public List<Group> load(Map<String, User> usersByDni) throws IOException {
        warnings.clear();
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            String[] f = lines.get(i).split(SEPARATOR, -1);
            if (f.length < 2) {
                warnings.add("skipped malformed group line " + (i + 1) + " in " + file);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (int m = 2; m < f.length; m++) {
                User member = usersByDni.get(f[m]);
                if (member == null) {
                    warnings.add("group " + f[0] + ": skipped unknown member " + f[m]);
                } else {
                    group.addMember(member);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            StringBuilder line = new StringBuilder(g.getIdentifier()).append(SEPARATOR).append(g.getName());
            for (User member : g.getMembers()) {
                line.append(SEPARATOR).append(member.getDni());
            }
            lines.add(line.toString());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
