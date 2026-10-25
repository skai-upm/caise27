package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file: id;name;dni1,dni2,...
 */
public class GroupRepository {

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads groups, linking members to existing users through their DNI.
     */
    public List<Group> load(Function<String, User> userByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(";", -1);
            if (f.length != 3) {
                System.err.println("WARNING skipping malformed group line: " + line);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(",")) {
                    User user = userByDni.apply(dni);
                    if (user != null) {
                        group.addUser(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Group g : groups) {
                List<String> dnis = new ArrayList<>();
                for (User u : g.getUsers()) {
                    dnis.add(u.getDni());
                }
                writer.write(g.getId() + ";" + g.getName() + ";" + String.join(",", dnis));
                writer.newLine();
            }
        }
    }
}
