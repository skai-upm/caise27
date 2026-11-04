package es.upm.park.business.service;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.UserFileStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupManagerTest {

    @TempDir
    Path dir;
    Path usersFile;
    Path groupsFile;
    UserManager users;
    GroupManager groups;

    @BeforeEach
    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @BeforeEach
    void setUp() {
        usersFile = dir.resolve("users.txt");
        groupsFile = dir.resolve("groups.txt");
        users = UserManager.initialize(new UserFileStorage(usersFile));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        groups = GroupManager.initialize(new GroupFileStorage(groupsFile), users);
    }

    private GroupManager restart() {
        ParkSession.resetControllers();
        users = UserManager.initialize(new UserFileStorage(usersFile));
        return GroupManager.initialize(new GroupFileStorage(groupsFile), users);
    }

    @Test
    void createAndDeleteGroup() {
        groups.create("G01", "Familia Garcia");
        Group g = groups.find("G01");
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMembers().isEmpty());
        groups.delete("G01");
        assertNull(groups.find("G01"));
    }

    @Test
    void duplicateOrBlankIdentifiersRejected() {
        groups.create("G01", "Familia Garcia");
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "Otro"));
        assertThrows(IllegalArgumentException.class, () -> groups.create(" ", "Otro"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("G02", ""));
        assertEquals("Familia Garcia", groups.find("G01").getName());
        assertEquals(1, groups.getGroups().size());
    }

    @Test
    void unknownGroupRejected() {
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G99"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G99", "ana@correo.com"));
    }

    @Test
    void addReferencesExistingUser() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ANA@correo.com");
        assertSame(users.findByEmail("ana@correo.com"), groups.find("G01").getMembers().get(0));
    }

    @Test
    void nonexistentAndDuplicateMembersRejected() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        List<String> before = Files.readAllLines(groupsFile);
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertEquals(1, groups.find("G01").getMembers().size());
        assertEquals(before, Files.readAllLines(groupsFile));
    }

    @Test
    void sameUserInSeveralGroups() {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        assertTrue(groups.find("G02").contains(users.findByEmail("ana@correo.com")));
    }

    @Test
    void removeMemberKeepsUser() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.removeMember("G01", "ana@correo.com");
        assertFalse(groups.find("G01").contains(users.findByEmail("ana@correo.com")));
        assertNotNull(users.findByEmail("ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "nadie@correo.com"));
    }

    @Test
    void deleteGroupKeepsUsers() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.delete("G01");
        assertNotNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void restartRestoresMembershipsAsReferences() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X"), Files.readAllLines(groupsFile));

        GroupManager again = restart();
        List<User> members = again.find("G01").getMembers();
        assertEquals(2, members.size());
        assertSame(users.findByDni("12345678Z"), members.get(0));
        assertSame(users.findByDni("87654321X"), members.get(1));
    }

    @Test
    void emailChangeSurvivesRestart() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertEquals("ana.nueva@correo.com", groups.find("G01").getMembers().get(0).getEmail());
        GroupManager again = restart();
        assertEquals("ana.nueva@correo.com", again.find("G01").getMembers().get(0).getEmail());
    }

    @Test
    void removeUserFromAllGroups() {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        groups.addMember("G02", "pedro@correo.com");
        User ana = users.delete("ana@correo.com", "Clave1!");
        groups.removeUserFromAllGroups(ana);
        assertTrue(groups.find("G01").getMembers().isEmpty());
        assertEquals(1, groups.find("G02").getMembers().size());
        assertEquals(1, restart().find("G02").getMembers().size());
    }
}
