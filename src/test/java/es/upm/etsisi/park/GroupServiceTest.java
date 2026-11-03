package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupServiceTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO);
    }

    private List<String> members(String group) {
        return f.park.findGroup(group).getMembers().stream().map(User::getEmail).toList();
    }

    @Test
    void createsEmptyGroup() {
        assertEquals("GROUP CREATED", f.run("GROUP CREATE;G01;Familia Garcia"));
        Group g = f.park.findGroup("G01");
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMembers().isEmpty());
    }

    @Test
    void rejectsDuplicateOrBlankGroup() {
        f.run("GROUP CREATE;G01;Familia Garcia");
        assertTrue(f.run("GROUP CREATE;G01;Otro").startsWith("ERROR: group G01 already exists"));
        assertEquals("Familia Garcia", f.park.findGroup("G01").getName());
        assertTrue(f.run("GROUP CREATE; ;Otro").startsWith("ERROR"));
        assertTrue(f.run("GROUP CREATE;G02;").startsWith("ERROR"));
    }

    @Test
    void addsMemberOnce() {
        f.run("GROUP CREATE;G01;Familia Garcia");
        assertEquals("USER ADDED TO GROUP", f.run("GROUP ADD;G01;ana@correo.com"));
        assertTrue(f.run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR: user ana@correo.com already belongs"));
        assertEquals(List.of("ana@correo.com"), members("G01"));
    }

    @Test
    void rejectsUnknownUserOrGroup() {
        f.run("GROUP CREATE;G01;Familia Garcia");
        assertTrue(f.run("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR: no user"));
        assertTrue(f.run("GROUP ADD;G99;ana@correo.com").startsWith("ERROR: no group"));
        assertTrue(f.run("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR: user ana@correo.com does not belong"));
        assertTrue(f.run("GROUP DELETE;G99").startsWith("ERROR: no group"));
        assertTrue(members("G01").isEmpty());
    }

    @Test
    void removesMember() {
        f.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com");
        assertEquals("USER REMOVED FROM GROUP", f.run("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals(List.of("pedro@correo.com"), members("G01"));
    }

    @Test
    void activeGroupTicketLocksGroupUntilDeleted() {
        f.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20");
        assertTrue(f.run("GROUP ADD;G01;pedro@correo.com").contains("locked"));
        assertTrue(f.run("GROUP REMOVE;G01;ana@correo.com").contains("locked"));
        assertTrue(f.run("GROUP DELETE;G01").contains("locked"));
        f.run("GROUPTICKET DELETE;TG01");
        assertEquals("USER ADDED TO GROUP", f.run("GROUP ADD;G01;pedro@correo.com"));
    }

    @Test
    void ticketForTodayLocksAndCannotBeDeleted() {
        f.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(f.run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(f.run("GROUP DELETE;G01").contains("locked"));
        assertNotNull(f.park.findGroup("G01"));
    }

    @Test
    void lockReleasedWhenDatePasses() {
        f.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(f.park.isGroupLocked(f.park.findGroup("G01")));
        f.run("SYSTEM DATE;2027-04-16");
        assertEquals("USER ADDED TO GROUP", f.run("GROUP ADD;G01;pedro@correo.com"));
        GroupTicket ticket = (GroupTicket) f.park.findTicket("TG01");
        assertEquals(1, ticket.getComposition().size());
    }

    @Test
    void deletingGroupKeepsHistoricalTickets() {
        f.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "SYSTEM DATE;2027-04-16");
        assertEquals("GROUP DELETED", f.run("GROUP DELETE;G01"));
        assertNull(f.park.findGroup("G01"));
        GroupTicket ticket = (GroupTicket) f.park.findTicket("TG01");
        assertNull(ticket.getGroup());
        assertEquals("40.00", ticket.getPrice().toPlainString());
        assertEquals("TG01:12345678Z", ticket.getComposition().get(0).getId());
    }
}
