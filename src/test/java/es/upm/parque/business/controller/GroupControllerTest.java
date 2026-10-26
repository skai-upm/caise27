package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestPark;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupControllerTest {

    @TempDir
    Path dir;

    private TestPark park;
    private User ana;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        park.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        ana = park.users.findByEmail("ana@correo.com");
    }

    @Test
    void createAndDelete() {
        park.groups.create("G01", "Familia Garcia");
        assertNotNull(park.groups.findById("G01"));
        assertThrows(ParkException.class, () -> park.groups.create("G01", "Otro"));
        assertThrows(ParkException.class, () -> park.groups.create("", "Otro"));
        assertThrows(ParkException.class, () -> park.groups.create("G02", " "));
        assertEquals(1, park.groups.getGroups().size());

        park.groups.addMember("G01", "ana@correo.com");
        park.groups.delete("G01");
        assertNull(park.groups.findById("G01"));
        assertNotNull(park.users.findByEmail("ana@correo.com"));
        assertThrows(ParkException.class, () -> park.groups.delete("G99"));
    }

    @Test
    void addMemberRules() {
        park.groups.create("G01", "F");
        park.groups.addMember("G01", "ana@correo.com");
        assertSame(ana, park.groups.findById("G01").getMembers().get(0));
        assertThrows(ParkException.class, () -> park.groups.addMember("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.groups.addMember("G01", "ANA@correo.com"));
        assertThrows(ParkException.class, () -> park.groups.addMember("G99", "pedro@correo.com"));
        assertEquals(List.of(ana), park.groups.findById("G01").getMembers());
    }

    @Test
    void removeMemberRules() {
        park.groups.create("G01", "F");
        park.groups.addMember("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.groups.removeMember("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.groups.removeMember("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.groups.removeMember("G99", "ana@correo.com"));
        park.groups.removeMember("G01", "ana@correo.com");
        assertTrue(park.groups.findById("G01").getMembers().isEmpty());
        assertNotNull(park.users.findByEmail("ana@correo.com"));
    }

    @Test
    void membersAreTheSameUserObjects() {
        park.groups.create("G01", "F");
        park.groups.addMember("G01", "ana@correo.com");
        park.users.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        assertEquals("Ana Maria", park.groups.findById("G01").getMembers().get(0).getName());
    }

    @Test
    void removeUserFromAllGroups() {
        park.groups.create("G01", "F");
        park.groups.create("G02", "Amigos");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G02", "ana@correo.com");
        park.groups.addMember("G02", "pedro@correo.com");
        park.groups.removeUserFromAllGroups(ana);
        park.groups.removeUserFromAllGroups(ana);
        TestPark restarted = new TestPark(dir);
        assertTrue(restarted.groups.findById("G01").getMembers().isEmpty());
        assertEquals(1, restarted.groups.findById("G02").getMembers().size());
        assertSame(restarted.users.findByEmail("pedro@correo.com"), restarted.groups.findById("G02").getMembers().get(0));
    }

    @Test
    void groupsSurviveRestartWithEmailChange() {
        park.groups.create("G01", "Familia Garcia");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G01", "pedro@correo.com");
        park.users.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");

        TestPark restarted = new TestPark(dir);
        Group g = restarted.groups.findById("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(2, g.getMembers().size());
        assertSame(restarted.users.findByEmail("ana.nueva@correo.com"), g.getMembers().get(0));
        assertSame(restarted.users.findByEmail("pedro@correo.com"), g.getMembers().get(1));
    }

    @Test
    void saveFailureRestoresPersistedState() {
        park.groups.create("G01", "F");
        Path file = dir.resolve("groups.txt");
        assertTrue(file.toFile().setWritable(false));
        try {
            assertThrows(ParkException.class, () -> park.groups.create("G02", "X"));
            assertNull(park.groups.findById("G02"));
            assertNotNull(park.groups.findById("G01"));
        } finally {
            assertTrue(file.toFile().setWritable(true));
        }
    }
}
