package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupServiceTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, ParkTestSupport.PEDRO);
    }

    private Group g01() {
        return s.park().getGroupService().findById("G01");
    }

    @Test
    void createAddRemoveDelete() {
        assertTrue(s.run("GROUP CREATE;G01;Familia Garcia").startsWith("OK"));
        assertEquals("Familia Garcia", g01().getName());
        assertTrue(g01().getMembers().isEmpty());
        assertTrue(s.run("GROUP ADD;G01;ana@correo.com").startsWith("OK"));
        assertEquals(1, g01().getMembers().size());
        assertTrue(s.run("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(g01().getMembers().isEmpty());
        assertTrue(s.run("GROUP DELETE;G01").startsWith("OK"));
        assertEquals(null, g01());
    }

    @Test
    void invalidOperationsAreRejected() {
        s.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com");
        assertTrue(s.run("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertEquals("Familia Garcia", g01().getName());
        assertTrue(s.run("GROUP CREATE;G02;").startsWith("ERROR"));
        assertTrue(s.run("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(s.run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(s.run("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertTrue(s.run("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertTrue(s.run("GROUP DELETE;G99").startsWith("ERROR"));
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void membersAreTheRegisteredUsers() {
        s.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        assertSame(s.park().getUserService().findByEmail("ana@correo.com"), g01().getMembers().get(0));
        assertEquals("Ana Maria", g01().getMembers().get(0).getName());
    }

    @Test
    void restartRestoresMemberReferences() throws IOException {
        s.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP CREATE;G02;Vacio");
        s.restart();
        Group g = g01();
        assertEquals(2, g.getMembers().size());
        assertSame(s.park().getUserService().findByEmail("ana@correo.com"), g.getMembers().get(0));
        assertSame(s.park().getUserService().findByEmail("pedro@correo.com"), g.getMembers().get(1));
        assertEquals(2, s.park().getUserService().getUsers().size());
        assertTrue(s.park().getGroupService().findById("G02").getMembers().isEmpty());
    }

    @Test
    void deletedUserIsRemovedFromGroups() throws IOException {
        s.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com");
        assertTrue(s.run("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertTrue(g01().getMembers().isEmpty());
        s.restart();
        assertTrue(g01().getMembers().isEmpty());
    }
}
