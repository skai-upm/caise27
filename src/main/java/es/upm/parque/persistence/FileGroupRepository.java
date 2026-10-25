package es.upm.parque.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.GroupRepository;

/**
 * Stores groups as {@code id;name;dni1,dni2,...}. Members are linked to existing users by DNI.
 */
public class FileGroupRepository implements GroupRepository {

    private static final Logger LOGGER = Logger.getLogger(FileGroupRepository.class.getName());

    private static final int FIELDS = 3;
    private static final String MEMBER_SEPARATOR = ",";

    private final Path file;

    public FileGroupRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Group> load(Function<String, User> userByDni) {
        List<Group> groups = new ArrayList<>();
        for (String[] f : TextFile.readRecords(file, FIELDS)) {
            Group group = new Group(f[0], f[1]);
            if (!f[2].isEmpty()) {
                for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                    User user = userByDni.apply(dni);
                    if (user == null) {
                        LOGGER.warning(() -> "group " + f[0] + " references unknown user " + dni);
                    } else {
                        group.addMember(user);
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void save(Collection<Group> groups) {
        TextFile.writeRecords(file, groups.stream()
                .map(g -> TextFile.join(g.getId(), g.getName(), String.join(MEMBER_SEPARATOR,
                        g.getMembers().stream().map(User::getDni).toList())))
                .toList());
    }
}
