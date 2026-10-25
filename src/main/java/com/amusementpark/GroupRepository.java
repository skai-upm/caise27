package com.amusementpark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}, one group per line.
 */
public class GroupRepository {
    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads the stored groups, linking members to the given users by DNI. Unknown DNIs are dropped.
     */
    public List<Group> load(List<User> users) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split(";", -1);
            if (fields.length != 3) {
                continue;
            }
            Group group = new Group(fields[0], fields[1]);
            if (!fields[2].isEmpty()) {
                for (String dni : fields[2].split(",")) {
                    User user = Repositories.findUser(users, dni);
                    if (user != null) {
                        group.add(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(group.getId() + ";" + group.getName() + ";" + String.join(",", dnis));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
