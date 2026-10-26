package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupManagerTest {

    @TempDir
    Path dir;

    private UserManager users;
    private GroupManager groups;

    @BeforeEach
    void setUp() throws IOException {
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
    }

    @Test
    void createRequiresUniqueNonEmptyIdAndName() {
        groups.create("G01", "Familia Garcia");
        assertThrows(UserException.class, () -> groups.create("G01", "Otra"));
        assertThrows(UserException.class, () -> groups.create("", "Otra"));
        assertThrows(UserException.class, () -> groups.create("G02", " "));
        assertEquals(1, groups.getGroups().size());
    }

    @Test
    void addUsesExistingUserObjects() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertSame(users.findByEmail("ana@correo.com"), groups.find("G01").getMembers().get(0));
        assertThrows(UserException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(UserException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(UserException.class, () -> groups.addMember("G99", "pedro@correo.com"));
        assertEquals(1, groups.find("G01").getMembers().size());
    }

    @Test
    void removeAndDelete() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertThrows(UserException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        groups.removeMember("G01", "ana@correo.com");
        assertTrue(groups.find("G01").getMembers().isEmpty());
        groups.delete("G01");
        assertNull(groups.find("G01"));
        assertThrows(UserException.class, () -> groups.delete("G01"));
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void groupsSurviveRestart() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");

        UserManager reloadedUsers = new UserManager(new UserRepository(dir.resolve("users.txt")));
        GroupManager reloaded = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), reloadedUsers);
        Group group = reloaded.find("G01");
        assertEquals("Familia Garcia", group.getName());
        assertSame(reloadedUsers.findByDni("12345678Z"), group.getMembers().get(0));
        assertSame(reloadedUsers.findByDni("87654321X"), group.getMembers().get(1));
    }

    @Test
    void removeFromAllGroups() {
        groups.create("G01", "A");
        groups.create("G02", "B");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        groups.removeFromAllGroups(users.findByEmail("ana@correo.com"));
        assertTrue(groups.find("G01").getMembers().isEmpty());
        assertTrue(groups.find("G02").getMembers().isEmpty());
    }
}
