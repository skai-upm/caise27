package es.upm.parque;

import es.upm.parque.business.model.Group;
import es.upm.parque.persistence.TextGroupRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GroupTest extends ParkTestSupport {

    private Group group(String id) {
        return groups().find(id);
    }

    @Test
    public void createsGroupsWithUniqueIds() {
        assertEquals("OK GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(group("G01").getMembers().isEmpty());
        assertEquals("Familia Garcia", group("G01").getName());
        fails("GROUP CREATE;G01;Otro");
        fails("GROUP CREATE;G02; ");
        fails("GROUP CREATE;G03");
    }

    @Test
    public void addsExistingUsersByReference() {
        createAna();
        ok("GROUP CREATE;G01;Familia Garcia");
        assertEquals("OK GROUP USER ADDED", run("GROUP ADD;G01;" + ANA));
        assertSame(user(ANA), group("G01").getMembers().get(0));
        ok("USER UPDATE;" + ANA + ";Clave1!;PHONE;699999999");
        assertEquals("699999999", group("G01").getMembers().get(0).getPhone());
    }

    @Test
    public void rejectsUnknownOrDuplicateMembersAndUnknownGroups() {
        createAna();
        ok("GROUP CREATE;G01;Familia Garcia");
        fails("GROUP ADD;G01;nadie@correo.com");
        ok("GROUP ADD;G01;" + ANA);
        fails("GROUP ADD;G01;" + ANA);
        fails("GROUP ADD;G99;" + ANA);
        assertEquals(1, group("G01").getMembers().size());
    }

    @Test
    public void removesMembers() {
        createAna();
        ok("GROUP CREATE;G01;Familia Garcia");
        fails("GROUP REMOVE;G01;" + ANA);
        ok("GROUP ADD;G01;" + ANA);
        assertEquals("OK GROUP USER REMOVED", run("GROUP REMOVE;G01;" + ANA));
        assertTrue(group("G01").getMembers().isEmpty());
    }

    @Test
    public void deletesGroupButNotUsers() {
        createAna();
        ok("GROUP CREATE;G01;Familia Garcia");
        ok("GROUP ADD;G01;" + ANA);
        assertEquals("OK GROUP DELETED", run("GROUP DELETE;G01"));
        assertNull(group("G01"));
        assertNotNull(user(ANA));
        fails("GROUP DELETE;G01");
    }

    @Test
    public void deletedUserLeavesGroups() {
        createAna();
        createPedro();
        ok("GROUP CREATE;G01;Familia Garcia");
        ok("GROUP ADD;G01;" + ANA);
        ok("GROUP ADD;G01;" + PEDRO);
        ok("USER DELETE;" + ANA + ";Clave1!");
        assertEquals(List.of(user(PEDRO)), group("G01").getMembers());
        restart();
        assertEquals(1, group("G01").getMembers().size());
    }

    @Test
    public void groupsSurviveRestartLinkedToRegisteredUsers() throws Exception {
        createAna();
        createPedro();
        ok("GROUP CREATE;G01;Familia Garcia");
        ok("GROUP ADD;G01;" + ANA);
        ok("GROUP ADD;G01;" + PEDRO);
        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X"),
                Files.readAllLines(dataDir.resolve(TextGroupRepository.FILE_NAME)));
        restart();
        Group restored = group("G01");
        assertEquals("Familia Garcia", restored.getName());
        assertSame(user(ANA), restored.getMembers().get(0));
        assertSame(user(PEDRO), restored.getMembers().get(1));
    }

    @Test
    public void malformedGroupLinesAreSkipped() throws Exception {
        Files.write(dataDir.resolve(TextGroupRepository.FILE_NAME), List.of("solo", "G01;Familia;99999999R"));
        restart();
        assertNull(group("solo"));
        assertNotNull(group("G01"));
        assertFalse(group("G01").getMembers().iterator().hasNext());
    }
}
