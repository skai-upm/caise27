package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line: {@code Id;Nombre;DNI1,DNI2,...}.
 * Members are stored by DNI and relinked to the loaded users when reading.
 */
public class GroupRepository {
    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    public List<Group> load(Function<String, User> userByDni) {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                if (f.length != 3) {
                    System.err.println("Ignoring malformed group line: " + line);
                    continue;
                }
                Group group = new Group(f[0], f[1]);
                if (!f[2].isEmpty()) {
                    for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                        User user = userByDni.apply(dni);
                        if (user == null) {
                            System.err.println("Ignoring unknown member " + dni + " of group " + f[0]);
                        } else {
                            group.addMember(user);
                        }
                    }
                }
                groups.add(group);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read groups file " + file + ": " + e.getMessage(), e);
        }
        return groups;
    }

    public void save(List<Group> groups) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (Group g : groups) {
                    List<String> dnis = new ArrayList<>();
                    for (User u : g.getMembers()) {
                        dnis.add(u.getDni());
                    }
                    writer.write(g.getId() + SEPARATOR + g.getName() + SEPARATOR + String.join(MEMBER_SEPARATOR, dnis));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write groups file " + file + ": " + e.getMessage(), e);
        }
    }
}
