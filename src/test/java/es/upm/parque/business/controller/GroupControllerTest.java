package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.UserField;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupControllerTest {

    @TempDir
    Path dir;
    private UserController users;
    private GroupController groups;

    @BeforeEach
    void setUp() {
        users = UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        groups = GroupController.getInstance().configure(new FileGroupRepository(dir.resolve("groups.txt")), users);
    }

    @Test
    void createValidatesIdentifierAndName() {
        groups.create("G01", "Familia Garcia");
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("", "Otra"));
        assertThrows(IllegalArgumentException.class, () -> groups.create("G02", " "));
        assertEquals("Familia Garcia", groups.findById("G01").getName());
    }

    @Test
    void addAndRemoveMembers() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertEquals(1, groups.findById("G01").getMembers().size());
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        groups.removeMember("G01", "ana@correo.com");
        assertEquals(0, groups.findById("G01").getMembers().size());
        assertNotNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void deleteKeepsUsers() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.delete("G01");
        assertNull(groups.findById("G01"));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
        assertNotNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void membersAreTheManagedUsers() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        assertEquals("Ana Maria", groups.findById("G01").getMembers().get(0).getName());
    }

    @Test
    void groupsAreRestoredWithMemberReferences() throws Exception {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.create("G02", "Vacio");
        assertEquals("G01;Familia Garcia;12345678Z,87654321X\nG02;Vacio;\n",
                Files.readString(dir.resolve("groups.txt")));
        UserController restartedUsers = UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")));
        GroupController restarted = GroupController.getInstance().configure(new FileGroupRepository(dir.resolve("groups.txt")), restartedUsers);
        Group g = restarted.findById("G01");
        assertEquals(2, g.getMembers().size());
        assertSame(restartedUsers.findByDni("12345678Z"), g.getMembers().get(0));
        assertEquals(0, restarted.findById("G02").getMembers().size());
    }
}
