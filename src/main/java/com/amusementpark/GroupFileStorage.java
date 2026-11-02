package com.amusementpark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists groups as {@code id;name;dni1,dni2,...}; members are resolved to existing users on load.
 */
public class GroupFileStorage {

    private static final String MEMBER_SEPARATOR = ",";

    private final LineFile file;

    public GroupFileStorage(Path file) {
        this.file = new LineFile(file);
    }

    public List<Group> load(List<User> users) throws IOException {
        List<Group> groups = new ArrayList<>();
        for (String[] f : file.read(3)) {
            Group group = new Group(f[0], f[1]);
            for (String dni : f[2].split(MEMBER_SEPARATOR)) {
                User member = findByDni(users, dni);
                if (member != null && !group.contains(member)) {
                    group.add(member);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    public void save(List<Group> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Group g : groups) {
            List<String> dnis = new ArrayList<>();
            for (User member : g.getMembers()) {
                dnis.add(member.getDni());
            }
            lines.add(LineFile.join(g.getId(), g.getName(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        file.write(lines);
    }

    static User findByDni(List<User> users, String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }
}
