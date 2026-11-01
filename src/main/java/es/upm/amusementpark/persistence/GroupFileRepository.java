package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.GroupRepository;

/**
 * Saves groups to and loads groups from a text file, one group per line:
 * {@code id;name;dni1,dni2,...}. Members are stored by DNI.
 */
public class GroupFileRepository implements GroupRepository {

    private final Path file;

    public GroupFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all groups, resolving member DNIs with the given lookup. Unknown DNIs
     * and malformed lines are skipped.
     */
    @Override
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
                continue;
            }
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(",")) {
                    User user = userByDni.apply(dni);
                    if (user != null && !group.hasMember(user)) {
                        group.addMember(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User u : g.getMembers()) {
                dnis.add(u.getDni());
            }
            lines.add(g.getId() + ";" + g.getName() + ";" + String.join(",", dnis));
        }
        TextFiles.writeAtomically(file, lines);
    }
}
