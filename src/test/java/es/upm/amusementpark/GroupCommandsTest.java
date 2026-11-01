package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

class GroupCommandsTest extends ParkTestSupport {

    @Test
    void createsEmptyGroup() {
        expect("OK: group G01 created", "GROUP CREATE;G01;Familia Garcia");
        Group group = park.findGroup("G01");
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.getMembers().isEmpty());
    }

    @Test
    void rejectsDuplicateOrIncompleteGroup() {
        ok("GROUP CREATE;G01;Familia Garcia");
        error("GROUP CREATE;G01;Otro");
        error("GROUP CREATE;G02");
        error("GROUP CREATE;G02;");
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
        assertEquals(1, park.getGroups().size());
    }

    @Test
    void addsAndRemovesExistingUsersByReference() {
        createUsers(2);
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + email(1));
        ok("GROUP ADD;G01;" + email(2));
        Group group = park.findGroup("G01");
        assertSame(park.getUserManager().findByEmail(email(1)), group.getMembers().get(0));

        ok("USER UPDATE;" + email(1) + ";Clave1!;NAME;Ana Maria");
        assertEquals("Ana Maria", group.getMembers().get(0).getName());

        ok("GROUP REMOVE;G01;" + email(1));
        assertEquals(List.of(park.getUserManager().findByEmail(email(2))), group.getMembers());
        assertTrue(park.getUserManager().findByEmail(email(1)) != null);
    }

    @Test
    void rejectsUnknownOrDuplicateMembers() {
        createUsers(1);
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + email(1));
        error("GROUP ADD;G01;nadie@correo.com");
        error("GROUP ADD;G01;" + email(1));
        error("GROUP ADD;G99;" + email(1));
        error("GROUP REMOVE;G01;nadie@correo.com");
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void deletesGroupKeepingUsers() {
        createUsers(1);
        createGroupWithUsers("G01", 1);
        expect("OK: group G01 deleted", "GROUP DELETE;G01");
        assertNull(park.findGroup("G01"));
        assertTrue(park.getUserManager().findByEmail(email(1)) != null);
        error("GROUP DELETE;G01");
    }

    @Test
    void membershipIsRestoredAfterRestart() throws IOException {
        createUsers(3);
        createGroupWithUsers("G01", 2);
        ok("GROUP CREATE;G02;Vacia");
        restart();

        Group group = park.findGroup("G01");
        assertEquals(2, group.getMembers().size());
        assertSame(park.getUserManager().findByEmail(email(1)), group.getMembers().get(0));
        assertSame(park.getUserManager().findByEmail(email(2)), group.getMembers().get(1));
        assertTrue(park.findGroup("G02").getMembers().isEmpty());
        assertEquals(3, park.getUserManager().getUsers().size());
    }

    @Test
    void groupFileStoresMembersByDni() throws IOException {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        assertEquals(List.of("G01;Group G01;" + dni(1) + "," + dni(2)), Files.readAllLines(tempDir.resolve("groups.txt")));
    }

    @Test
    void failedSaveLeavesGroupsUnchanged() throws IOException {
        createUsers(1);
        ok("GROUP CREATE;G01;Familia");
        Files.delete(tempDir.resolve("groups.txt"));
        Files.createDirectory(tempDir.resolve("groups.txt"));
        error("GROUP ADD;G01;" + email(1));
        error("GROUP CREATE;G02;Otra");
        assertTrue(park.findGroup("G01").getMembers().isEmpty());
        assertNull(park.findGroup("G02"));
    }
}
