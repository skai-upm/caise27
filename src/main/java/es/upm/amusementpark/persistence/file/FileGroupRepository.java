package es.upm.amusementpark.persistence.file;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.persistence.PersistenceException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and retrieves groups. One group per line: {@code id;name;dni1,dni2,...}; members are linked to the
 * registered users by DNI when loading.
 */
public class FileGroupRepository implements GroupRepository {

    public static final String FILE_NAME = "groups.txt";

    private final Path file;
    private final UserRepository users;
    private final List<Group> groups = new ArrayList<>();

    public FileGroupRepository(Path file, UserRepository users) {
        this.file = file;
        this.users = users;
    }

    public void load() {
        groups.clear();
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(";", -1);
                if (fields.length != 3) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                    continue;
                }
                Group group = new Group(fields[0], fields[1]);
                if (!fields[2].isEmpty()) {
                    for (String dni : fields[2].split(",")) {
                        User member = users.findByDni(dni);
                        if (member == null) {
                            System.err.println("WARNING: unknown member " + dni + " in group " + fields[0]);
                        } else {
                            group.addMember(member);
                        }
                    }
                }
                groups.add(group);
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot read groups file " + file, e);
        }
    }

    @Override
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Group group : groups) {
                List<String> dnis = new ArrayList<>();
                for (User member : group.getMembers()) {
                    dnis.add(member.getDni());
                }
                writer.write(group.getId() + ";" + group.getName() + ";" + String.join(",", dnis));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot write groups file " + file, e);
        }
    }

    @Override
    public List<Group> findAll() {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public Group findById(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    @Override
    public void add(Group group) {
        groups.add(group);
        save();
    }

    @Override
    public void remove(Group group) {
        groups.remove(group);
        save();
    }
}
