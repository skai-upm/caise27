package es.upm.etsisi.amusementpark.business.service;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupManagerTest {

    @TempDir
    Path dir;

    private Path usersFile;
    private Path groupsFile;
    private UserManager users;
    private GroupManager groups;

    @BeforeEach
    void setUp() throws Exception {
        usersFile = dir.resolve("users.txt");
        groupsFile = dir.resolve("groups.txt");
        users = new UserManager(new UserFileRepository(usersFile), TestSupport.HASHER);
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        groups = new GroupManager(new GroupFileRepository(groupsFile), users);
    }

    private User ana() {
        return users.findByEmail("ana@correo.com").orElseThrow();
    }

    @Test
    void createAndRejectDuplicatesOrBlanks() throws Exception {
        groups.create("G01", "Familia Garcia");
        Group g = groups.find("G01").orElseThrow();
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMembers().isEmpty());

        assertThrows(UserException.class, () -> groups.create("G01", "Otra"));
        assertThrows(UserException.class, () -> groups.create(" ", "Familia"));
        assertThrows(UserException.class, () -> groups.create("G02", ""));
        assertEquals("Familia Garcia", groups.find("G01").orElseThrow().getName());
        assertEquals(1, groups.getGroups().size());
    }

    @Test
    void addsExistingUsersByReference() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertSame(ana(), groups.find("G01").orElseThrow().getMembers().get(0));
    }

    @Test
    void rejectsDuplicateUnknownUserAndUnknownGroup() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        String before = Files.readString(groupsFile);

        assertThrows(UserException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(UserException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(UserException.class, () -> groups.addMember("G99", "ana@correo.com"));

        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
        assertEquals(before, Files.readString(groupsFile));
    }

    @Test
    void sameUserInTwoGroups() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        assertTrue(groups.find("G02").orElseThrow().contains(ana()));
    }

    @Test
    void removeMemberKeepsUser() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.removeMember("G01", "ana@correo.com");
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
        assertTrue(users.findByEmail("ana@correo.com").isPresent());

        assertThrows(UserException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertThrows(UserException.class, () -> groups.removeMember("G01", "nadie@correo.com"));
        assertThrows(UserException.class, () -> groups.removeMember("G99", "ana@correo.com"));
    }

    @Test
    void deleteGroupKeepsUsers() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.delete("G01");
        assertFalse(groups.find("G01").isPresent());
        assertEquals(2, users.getUsers().size());
        assertThrows(UserException.class, () -> groups.delete("G99"));
    }

    @Test
    void membershipFollowsEmailChange() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        groups.removeMember("G01", "ana.nueva@correo.com");
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
    }

    @Test
    void removeUserEverywhere() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G02", "ana@correo.com");
        User ana = users.delete("ana@correo.com", "Clave1!");
        groups.removeUserEverywhere(ana);

        UserManager restartedUsers = new UserManager(new UserFileRepository(usersFile), TestSupport.HASHER);
        GroupManager restarted = new GroupManager(new GroupFileRepository(groupsFile), restartedUsers);
        assertEquals(1, restarted.find("G01").orElseThrow().getMembers().size());
        assertTrue(restarted.find("G02").orElseThrow().getMembers().isEmpty());
    }

    @Test
    void restartRestoresGroupsWithSameUserInstances() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G02", "ana@correo.com");

        UserManager restartedUsers = new UserManager(new UserFileRepository(usersFile), TestSupport.HASHER);
        GroupManager restarted = new GroupManager(new GroupFileRepository(groupsFile), restartedUsers);
        User restartedAna = restartedUsers.findByEmail("ana@correo.com").orElseThrow();
        assertEquals(2, restarted.find("G01").orElseThrow().getMembers().size());
        assertSame(restartedAna, restarted.find("G01").orElseThrow().getMembers().get(0));
        assertSame(restartedAna, restarted.find("G02").orElseThrow().getMembers().get(0));
        assertEquals(2, restartedUsers.getUsers().size());
    }

    @Test
    void failedSaveRollsBack() throws Exception {
        Path plain = dir.resolve("plain");
        Files.writeString(plain, "x");
        GroupManager broken = new GroupManager(new GroupFileRepository(plain.resolve("groups.txt")), users);
        assertThrows(UserException.class, () -> broken.create("G01", "Familia"));
        assertTrue(broken.getGroups().isEmpty());
    }
}
