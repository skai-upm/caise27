package es.upm.etsisi.park.persistence.text;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.persistence.GroupRepository;

/**
 * Stores groups one per line: {@code id;name;dni1,dni2,...}. Members are
 * resolved by DNI against the active users when loading.
 */
public class TextGroupRepository implements GroupRepository {

    public static final String FILE_NAME = "groups.txt";

    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public TextGroupRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Group> load(Map<String, User> usersByDni) throws IOException {
        List<Group> groups = new ArrayList<>();
        List<String> lines = TextFiles.readLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = TextFiles.fields(line);
            if (fields.length != FIELD_COUNT) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file);
                continue;
            }
            Group group = new Group(fields[0], fields[1]);
            for (String dni : fields[2].split(MEMBER_SEPARATOR)) {
                if (dni.isEmpty()) {
                    continue;
                }
                User user = usersByDni.get(dni);
                if (user == null) {
                    warnings.println("WARNING: unknown member " + dni + " of group " + group.getId() + " ignored");
                } else {
                    group.addMember(user);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : group.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(String.join(TextFiles.SEPARATOR, group.getId(), group.getName(),
                    String.join(MEMBER_SEPARATOR, dnis)));
        }
        TextFiles.write(file, lines);
    }
}
