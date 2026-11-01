package es.upm.amusementpark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores groups in a text file, one group per line: {@code id;name;dni1,dni2,...}.
 * Members are stored by DNI and resolved to the registered users when loading.
 */
public class GroupRepository {

    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    public List<Group> load(Map<String, User> usersByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(SEPARATOR, -1);
            if (parts.length != 3) {
                System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                continue;
            }
            Group group = new Group(parts[0], parts[1]);
            for (String dni : parts[2].split(MEMBER_SEPARATOR)) {
                User user = usersByDni.get(dni);
                if (user != null) {
                    group.addMember(user);
                } else if (!dni.isEmpty()) {
                    System.err.println("WARNING: unknown member " + dni + " in group " + parts[0]);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
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
