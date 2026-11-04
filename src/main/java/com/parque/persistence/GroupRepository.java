package com.parque.persistence;

import com.parque.business.model.Group;
import com.parque.business.model.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file, one group per line: {@code Identificador;Nombre;DNI1,DNI2,...}.
 * Members are stored by DNI (immutable) and resolved to the registered users when loading.
 */
public class GroupRepository {
    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public GroupRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads every well-formed group; {@code userByDni} returns the registered user for a DNI or null.
     * Unknown DNIs and malformed lines are skipped. A missing file means no groups.
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
            if (f.length != 3) {
                System.err.println("Skipping malformed group line " + lineNumber + " in " + file);
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
        return groups;
    }

    /** Rewrites the file with exactly the given groups. */
    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : g.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(g.getId() + SEPARATOR + g.getName() + SEPARATOR + String.join(MEMBER_SEPARATOR, dnis));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
