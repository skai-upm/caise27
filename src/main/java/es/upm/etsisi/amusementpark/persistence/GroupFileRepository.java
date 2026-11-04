package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line: {@code Id;Nombre;DNI1;DNI2;...}.
 * Members are stored by DNI and linked back to existing users on load.
 */
public class GroupFileRepository {

    private static final String SEPARATOR = ";";

    private final Path file;

    public GroupFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all groups; a missing file means there are no groups.
     *
     * @param userByDni returns the existing user with a DNI, or {@code null} if none
     */
    public List<Group> load(Function<String, User> userByDni) throws IOException {
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
            String[] f = line.split(SEPARATOR, -1);
            if (f.length < 2) {
                System.err.println("Skipping malformed group record at line " + lineNumber + " of " + file);
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (int i = 2; i < f.length; i++) {
                User user = userByDni.apply(f[i]);
                if (user == null) {
                    System.err.println("Ignoring unknown member " + f[i] + " of group " + f[0]);
                } else {
                    group.addMember(user);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    /** Replaces the file content with the given groups. */
    public void save(Collection<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>(groups.size());
        for (Group g : groups) {
            List<String> fields = new ArrayList<>();
            fields.add(g.getId());
            fields.add(g.getName());
            for (User u : g.getMembers()) {
                fields.add(u.getDni());
            }
            lines.add(String.join(SEPARATOR, fields));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
