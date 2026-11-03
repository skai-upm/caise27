package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupControllerTest {

    @TempDir
    Path dir;
    Path groupsFile;
    UserController users;
    GroupController groups;

    @BeforeEach
    void setUp() {
        users = new UserController(new UserFileStorage(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        groupsFile = dir.resolve("groups.txt");
        groups = new GroupController(new GroupFileStorage(groupsFile), users);
        groups.create("G01", "Familia Garcia");
    }

    @Test
    void createsGroupsWithUniqueNonBlankIds() {
        assertEquals("Familia Garcia", groups.find("G01").orElseThrow().getName());
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("G02", " "));
        assertEquals(1, groups.getGroups().size());
        assertEquals("Familia Garcia", groups.find("G01").orElseThrow().getName());
    }

    @Test
    void addsRegisteredUsersAsReferences() {
        groups.addMember("G01", "ana@correo.com");
        User member = groups.find("G01").orElseThrow().getMembers().get(0);
        assertSame(users.findByEmail("ana@correo.com").orElseThrow(), member);
    }

    @Test
    void rejectsUnknownUserDuplicateMemberAndUnknownGroup() throws IOException {
        groups.addMember("G01", "ana@correo.com");
        List<String> before = Files.readAllLines(groupsFile);
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
        assertEquals(before, Files.readAllLines(groupsFile));
    }

    @Test
    void removesMembersWithoutDeletingUsers() {
        groups.addMember("G01", "ana@correo.com");
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G99", "ana@correo.com"));
        groups.removeMember("G01", "ana@correo.com");
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
        assertTrue(users.findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void deletesGroupsWithoutDeletingUsers() {
        groups.addMember("G01", "ana@correo.com");
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G99"));
        groups.delete("G01");
        assertTrue(groups.find("G01").isEmpty());
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void updatedUserIsSeenThroughTheGroup() {
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        groups.removeMember("G01", "ana.nueva@correo.com");
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
    }

    @Test
    void removeUserFromAllGroups() throws IOException {
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        groups.addMember("G02", "pedro@correo.com");
        User ana = users.delete("ana@correo.com", "Clave1!");
        groups.removeUserFromAll(ana);
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
        assertEquals(List.of("G01;Familia Garcia;", "G02;Amigos;87654321X"), Files.readAllLines(groupsFile));
    }

    @Test
    void failedSaveRevertsChanges() throws IOException {
        groups.addMember("G01", "ana@correo.com");
        Files.delete(groupsFile);
        Files.createDirectory(groupsFile);
        assertThrows(RuntimeException.class, () -> groups.create("G02", "X"));
        assertThrows(RuntimeException.class, () -> groups.addMember("G01", "pedro@correo.com"));
        assertThrows(RuntimeException.class, () -> groups.removeMember("G01", "ana@correo.com"));
        assertThrows(RuntimeException.class, () -> groups.delete("G01"));
        assertEquals(1, groups.getGroups().size());
        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
    }
}
