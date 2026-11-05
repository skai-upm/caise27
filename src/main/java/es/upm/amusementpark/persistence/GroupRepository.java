package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.Group;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Persists groups as {@code id;name[;memberEmail...]}.
 */
public class GroupRepository {

    private final TextFile file;

    public GroupRepository(Path path) {
        this.file = new TextFile(path);
    }

    public void load(ParkController park, List<String> warnings) throws IOException {
        List<String> lines = file.readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFile.fields(line);
            try {
                if (f.length < 2) {
                    throw new IllegalArgumentException("expected at least 2 fields");
                }
                park.restoreGroup(f[0], f[1], Arrays.asList(f).subList(2, f.length));
            } catch (IllegalArgumentException | ParkException e) {
                warnings.add(file.getPath().getFileName() + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public void save(ParkController park) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group group : park.getGroups()) {
            List<String> fields = new ArrayList<>();
            fields.add(group.getId());
            fields.add(group.getName());
            fields.addAll(group.getMembers());
            lines.add(TextFile.join(fields.toArray(new String[0])));
        }
        file.writeLines(lines);
    }
}
