package com.amusementpark.presentation;

import com.amusementpark.business.controller.GroupController;
import com.amusementpark.business.controller.UserController;
import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupManagementTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.user("Pedro", 87654321);
    }

    private Group g01() {
        return GroupController.getInstance().find("G01");
    }

    @Test
    void createAddRemoveDelete() {
        assertEquals("OK: Group created: G01", f.run("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("Familia Garcia", g01().getName());
        f.ok("GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com");
        assertEquals(2, g01().getMembers().size());
        assertSame(UserController.getInstance().findByEmail("ana@correo.com"), g01().getMembers().get(0));

        f.ok("GROUP REMOVE;G01;ana@correo.com");
        assertEquals(1, g01().getMembers().size());
        assertTrue(UserController.getInstance().findByEmail("ana@correo.com") != null);

        f.ok("GROUP DELETE;G01");
        assertEquals(null, g01());
        assertEquals(2, UserController.getInstance().getUsers().size());
    }

    @Test
    void rejectsInvalidOperationsWithoutChanges() throws IOException {
        f.ok("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com");
        List<String> before = Files.readAllLines(dir.resolve("groups.txt"));
        assertTrue(f.run("GROUP CREATE;G01;Otro").startsWith("ERROR:"));
        assertTrue(f.run("GROUP CREATE;;Otro").startsWith("ERROR:"));
        assertTrue(f.run("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("GROUP ADD;G99;pedro@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("GROUP DELETE;G99").startsWith("ERROR:"));
        assertEquals("Familia Garcia", g01().getName());
        assertEquals(1, g01().getMembers().size());
        assertEquals(before, Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void membershipSharesUserObjects() {
        f.ok("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        assertEquals("Ana Maria", g01().getMembers().get(0).getName());
    }

    @Test
    void restoresMembershipOnRestart() throws IOException {
        f.ok("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP CREATE;G02;Vacio");
        f.restart();
        List<User> users = UserController.getInstance().getUsers();
        assertEquals(2, users.size());
        assertSame(users.get(0), g01().getMembers().get(0));
        assertSame(users.get(1), g01().getMembers().get(1));
        assertTrue(GroupController.getInstance().find("G02").getMembers().isEmpty());
    }

    @Test
    void deletedUserLeavesGroups() throws IOException {
        f.ok("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "USER DELETE;ana@correo.com;Clave1!");
        assertTrue(g01().getMembers().isEmpty());
        f.restart();
        assertTrue(g01().getMembers().isEmpty());
    }
}
