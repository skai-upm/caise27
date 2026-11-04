package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores groups in a text file: {@code id;name;dni1;dni2;...}, one group per line.
 * Members are written by DNI and resolved to the already loaded users on load.
 */
public class GroupFileStorage {

    private static final String SEPARATOR = ";";

    private final Path file;

    public GroupFileStorage(Path file) {
        this.file = file;
    }

    /**
     * Loads the groups, resolving each member DNI with {@code userByDni}. Unknown DNIs,
     * malformed and blank lines are ignored; a missing file means no groups.
     */
    public List<Group> load(Function<String, User> userByDni) {
        List<Group> groups = new ArrayList<>();
        if (!Files.exists(file)) {
            return groups;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Group group = parse(line, userByDni);
                if (group != null) {
                    groups.add(group);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read groups file " + file, e);
        }
        return groups;
    }

    public void save(Collection<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            lines.add(format(g));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write groups file " + file, e);
        }
    }

    static String format(Group g) {
        StringBuilder sb = new StringBuilder(g.getId()).append(SEPARATOR).append(g.getName());
        for (User u : g.getMembers()) {
            sb.append(SEPARATOR).append(u.getDni());
        }
        return sb.toString();
    }

    private static Group parse(String line, Function<String, User> userByDni) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if (f.length < 2 || f[0].isBlank()) {
            return null;
        }
        Group group = new Group(f[0], f[1]);
        for (int i = 2; i < f.length; i++) {
            User user = userByDni.apply(f[i]);
            if (user != null && !group.contains(user)) {
                group.add(user);
            }
        }
        return group;
    }
}
