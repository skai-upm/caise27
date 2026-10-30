package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}, one group per line. Members are resolved by DNI.
 */
public class FileGroupRepository implements GroupRepository {

    private final Path file;

    public FileGroupRepository(Path file) {
        this.file = file;
    }

    public List<Group> load(List<User> users) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] f = line.split(";", -1);
            if (f.length != 3) {
                System.err.println("WARNING: skipping malformed group record: " + line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (String dni : f[2].split(",")) {
                User user = findByDni(users, dni);
                if (user != null) {
                    group.addMember(user);
                } else if (!dni.isEmpty()) {
                    System.err.println("WARNING: group " + f[0] + " references unknown user " + dni);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User u : g.getMembers()) {
                dnis.add(u.getDni());
            }
            lines.add(g.getId() + ";" + g.getName() + ";" + String.join(",", dnis));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    static User findByDni(List<User> users, String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }
}
