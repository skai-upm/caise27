package com.parque.business.service;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupServiceTest {
    @TempDir
    Path dir;
    UserService users;
    GroupService groups;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserService(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia Garcia");
    }

    private List<String> groupLines() throws Exception {
        return Files.readAllLines(dir.resolve("groups.txt"));
    }

    @Test
    void createRules() throws Exception {
        Group g = groups.find("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(0, g.getMembers().size());
        List<String> before = groupLines();
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create(" ", "Familia"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("G02", ""));
        assertEquals(before, groupLines());
        assertEquals(1, groups.getGroups().size());
    }

    @Test
    void addExistingUserOnlyOnce() throws Exception {
        groups.addMember("G01", "ana@correo.com");
        assertSame(users.findByEmail("ana@correo.com"), groups.find("G01").getMembers().get(0));
        List<String> before = groupLines();
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertEquals(before, groupLines());
        assertEquals(1, groups.find("G01").getMembers().size());
    }

    @Test
    void userInSeveralGroups() {
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        assertSame(groups.find("G01").getMembers().get(0), groups.find("G02").getMembers().get(0));
    }

    @Test
    void removeMember() throws Exception {
        groups.addMember("G01", "ana@correo.com");
        groups.removeMember("G01", "ana@correo.com");
        assertEquals(0, groups.find("G01").getMembers().size());
        assertNotNull(users.findByEmail("ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G99", "ana@correo.com"));
        assertEquals(List.of("G01;Familia Garcia;"), groupLines());
    }

    @Test
    void deleteGroupKeepsUsers() throws Exception {
        groups.addMember("G01", "ana@correo.com");
        groups.delete("G01");
        assertNull(groups.find("G01"));
        assertNotNull(users.findByEmail("ana@correo.com"));
        assertEquals(List.of(), groupLines());
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
    }

    @Test
    void memberSeesUserUpdates() {
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        assertEquals("699999999", groups.find("G01").getMembers().get(0).getPhone());
    }

    @Test
    void removeUserFromAllGroups() throws Exception {
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G02", "ana@correo.com");
        User ana = users.delete("ana@correo.com", "Clave1!");
        groups.removeUserFromAllGroups(ana);
        assertEquals(List.of("G01;Familia Garcia;87654321X", "G02;Amigos;"), groupLines());
    }

    @Test
    void restartRestoresSharedReferencesAndSurvivesEmailChange() throws Exception {
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G02", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");

        UserService users2 = new UserService(new UserRepository(dir.resolve("users.txt")));
        GroupService groups2 = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users2);
        User ana = users2.findByEmail("ana.nueva@correo.com");
        assertSame(ana, groups2.find("G01").getMembers().get(0));
        assertSame(ana, groups2.find("G02").getMembers().get(0));
        assertSame(users2.findByEmail("pedro@correo.com"), groups2.find("G01").getMembers().get(1));
        groups2.removeMember("G01", "ana.nueva@correo.com");
        assertTrue(groups2.find("G02").contains(ana));
    }
}
