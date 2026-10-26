package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.persistence.GroupStorage;
import es.upm.parque.persistence.UserStorage;
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
    void setUp() {
        users = new UserManager(new UserStorage(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        groups = new GroupManager(users, new GroupStorage(dir.resolve("groups.txt")));
        groups.create("G01", "Familia Garcia");
    }

    @Test
    void createRejectsDuplicatesAndBlanks() {
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create(" ", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("G02", ""));
        assertEquals("Familia Garcia", groups.find("G01").getName());
    }

    @Test
    void addUsesTheRegisteredUserObject() {
        groups.addMember("G01", "ana@correo.com");
        assertSame(users.findByEmail("ana@correo.com"), groups.find("G01").getMembers().get(0));
        users.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        assertEquals("Ana Maria", groups.find("G01").getMembers().get(0).getName());
    }

    @Test
    void addRejectsUnknownUsersGroupsAndDuplicates() {
        groups.addMember("G01", "ana@correo.com");
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertEquals(1, groups.find("G01").getMembers().size());
    }

    @Test
    void removeAndDelete() {
        groups.addMember("G01", "ana@correo.com");
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        groups.removeMember("G01", "ana@correo.com");
        assertTrue(groups.find("G01").getMembers().isEmpty());
        assertTrue(users.findByEmail("ana@correo.com") != null);
        groups.delete("G01");
        assertNull(groups.find("G01"));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
    }

    @Test
    void restartRestoresGroupsWithRegisteredUsers() {
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");

        UserManager reloadedUsers = new UserManager(new UserStorage(dir.resolve("users.txt")));
        GroupManager reloaded = new GroupManager(reloadedUsers, new GroupStorage(dir.resolve("groups.txt")));
        Group g = reloaded.find("G01");
        assertEquals(2, g.getMembers().size());
        assertSame(reloadedUsers.findByDni("12345678Z"), g.getMembers().get(0));
        assertSame(reloadedUsers.findByDni("87654321X"), g.getMembers().get(1));
    }

    @Test
    void removeUserFromAllGroups() {
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        User ana = users.delete("ana@correo.com", "Clave1!");
        groups.removeUserFromAll(ana);
        GroupManager reloaded = new GroupManager(users, new GroupStorage(dir.resolve("groups.txt")));
        assertTrue(reloaded.find("G01").getMembers().isEmpty());
        assertTrue(reloaded.find("G02").getMembers().isEmpty());
    }
}
