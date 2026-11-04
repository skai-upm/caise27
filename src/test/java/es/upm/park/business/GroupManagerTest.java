package es.upm.park.business;

import es.upm.park.business.model.Group;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.persistence.UserFileStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    void setUp() throws IOException {
        usersFile = dir.resolve("users.txt");
        groupsFile = dir.resolve("groups.txt");
        users = new UserManager(new UserFileStore(usersFile));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        groups = new GroupManager(new GroupFileStore(groupsFile), users);
    }

    private List<String> groupLines() throws IOException {
        return Files.exists(groupsFile) ? Files.readAllLines(groupsFile) : List.of();
    }

    private Group g(String id) {
        return groups.find(id).orElseThrow();
    }

    @Test
    void createGroup() throws IOException {
        assertTrue(groups.create("G01", "Familia Garcia").success());
        assertEquals("Familia Garcia", g("G01").getName());
        assertEquals(List.of("G01;Familia Garcia"), groupLines());
    }

    @Test
    void duplicateOrBlankGroupRejected() throws IOException {
        groups.create("G01", "Familia Garcia");
        assertFalse(groups.create("G01", "Otra").success());
        assertFalse(groups.create(" ", "Otra").success());
        assertFalse(groups.create("G02", "").success());
        assertEquals("Familia Garcia", g("G01").getName());
        assertEquals(1, groups.getGroups().size());
        assertEquals(List.of("G01;Familia Garcia"), groupLines());
    }

    @Test
    void addRegisteredUserRefersToSameObject() {
        groups.create("G01", "Familia Garcia");
        assertTrue(groups.addMember("G01", "ana@correo.com").success());
        assertSame(users.findByEmail("ana@correo.com").orElseThrow(), g("G01").getMembers().get(0));
    }

    @Test
    void nonexistentUserOrGroupRejected() throws IOException {
        groups.create("G01", "Familia Garcia");
        List<String> before = groupLines();
        assertFalse(groups.addMember("G01", "nadie@correo.com").success());
        assertFalse(groups.addMember("G99", "ana@correo.com").success());
        assertTrue(g("G01").getMembers().isEmpty());
        assertEquals(before, groupLines());
    }

    @Test
    void duplicateMembershipRejected() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        List<String> before = groupLines();
        assertFalse(groups.addMember("G01", "ANA@correo.com").success());
        assertEquals(1, g("G01").getMembers().size());
        assertEquals(before, groupLines());
    }

    @Test
    void sameUserInSeveralGroups() {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        assertTrue(groups.addMember("G01", "ana@correo.com").success());
        assertTrue(groups.addMember("G02", "ana@correo.com").success());
        assertSame(g("G01").getMembers().get(0), g("G02").getMembers().get(0));
    }

    @Test
    void removeMemberKeepsUserRegistered() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertTrue(groups.removeMember("G01", "ana@correo.com").success());
        assertTrue(g("G01").getMembers().isEmpty());
        assertTrue(users.findByEmail("ana@correo.com").isPresent());
        assertFalse(groups.removeMember("G01", "ana@correo.com").success());
        assertFalse(groups.removeMember("G01", "nadie@correo.com").success());
        assertFalse(groups.removeMember("G99", "ana@correo.com").success());
    }

    @Test
    void deleteGroupKeepsUsers() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        assertTrue(groups.delete("G01").success());
        assertTrue(groups.find("G01").isEmpty());
        assertEquals(2, users.getUsers().size());
        assertTrue(groupLines().isEmpty());
        assertFalse(groups.delete("G01").success());
    }

    @Test
    void userUpdateVisibleThroughGroup() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertEquals("ana.nueva@correo.com", g("G01").getMembers().get(0).getEmail());
        assertTrue(groups.removeMember("G01", "ana.nueva@correo.com").success());
    }

    @Test
    void removeUserEverywhere() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G02", "ana@correo.com");
        User ana = users.findByEmail("ana@correo.com").orElseThrow();
        assertTrue(groups.removeUserEverywhere(ana).success());
        assertFalse(g("G01").contains(ana));
        assertFalse(g("G02").contains(ana));
        assertEquals(List.of("G01;Familia Garcia;87654321X", "G02;Amigos"), groupLines());
    }

    @Test
    void restartRestoresRelationshipsWithSharedUsers() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G02", "ana@correo.com");

        UserManager users2 = new UserManager(new UserFileStore(usersFile));
        GroupManager groups2 = new GroupManager(new GroupFileStore(groupsFile), users2);
        Group g1 = groups2.find("G01").orElseThrow();
        Group g2 = groups2.find("G02").orElseThrow();
        User ana = users2.findByEmail("ana@correo.com").orElseThrow();
        assertEquals(2, g1.getMembers().size());
        assertSame(ana, g1.getMembers().get(0));
        assertSame(ana, g2.getMembers().get(0));
        assertEquals("Familia Garcia", g1.getName());
    }
}
