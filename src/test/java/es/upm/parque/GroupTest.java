package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, TODAY);
        t.run(ParkTestSupport.CREATE_ANA, ParkTestSupport.CREATE_PEDRO);
    }

    @Test
    void createAddRemoveDelete() {
        assertEquals("OK GROUP CREATED", t.run("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("OK GROUP MEMBER ADDED", t.run("GROUP ADD;G01;ana@correo.com"));
        assertEquals("OK GROUP MEMBER ADDED", t.run("GROUP ADD;G01;pedro@correo.com"));
        Group g = t.groups.get("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(2, g.getMembers().size());
        assertEquals("OK GROUP MEMBER REMOVED", t.run("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals(1, g.getMembers().size());
        assertEquals("OK GROUP DELETED", t.run("GROUP DELETE;G01"));
        assertNull(t.groups.find("G01"));
        assertEquals(2, t.users.getUsers().size());
    }

    @Test
    void rejectsInvalidOperations() {
        t.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com");
        assertTrue(t.run("GROUP CREATE;G01;Otro").startsWith("ERROR"));
        assertTrue(t.run("GROUP CREATE;G 02;Otro").startsWith("ERROR"));
        assertTrue(t.run("GROUP CREATE;G02; ").startsWith("ERROR"));
        assertTrue(t.run("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(t.run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(t.run("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertTrue(t.run("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertTrue(t.run("GROUP DELETE;G99").startsWith("ERROR"));
        assertEquals(1, t.groups.get("G01").getMembers().size());
    }

    @Test
    void membersAreTheSameUserObjects() {
        t.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        User member = t.groups.get("G01").getMembers().get(0);
        assertSame(t.users.findByEmail("ana@correo.com"), member);
        assertEquals("Ana Maria", member.getName());
    }

    @Test
    void groupsAreRestoredLinkedToRestoredUsers() throws IOException {
        t.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP CREATE;G02;Vacia");
        assertEquals("G01;Familia Garcia;12345678Z,87654321X" + System.lineSeparator() + "G02;Vacia;"
                + System.lineSeparator(), Files.readString(dir.resolve("groups.txt")));
        ParkTestSupport restarted = t.restart(TODAY);
        Group g = restarted.groups.get("G01");
        assertSame(restarted.users.findByDni("12345678Z"), g.getMembers().get(0));
        assertTrue(restarted.groups.get("G02").getMembers().isEmpty());
    }

    @Test
    void unknownMemberInFileIsSkipped() throws IOException {
        Files.writeString(dir.resolve("groups.txt"), "G01;Familia;12345678Z,99999999R\n");
        Group g = t.restart(TODAY).groups.get("G01");
        assertNotNull(g);
        assertEquals(1, g.getMembers().size());
    }

    @Test
    void deletedUserLeavesItsGroups() {
        t.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com");
        assertEquals("OK USER DELETED", t.run("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(1, t.groups.get("G01").getMembers().size());
        assertEquals(1, t.restart(TODAY).groups.get("G01").getMembers().size());
    }
}
