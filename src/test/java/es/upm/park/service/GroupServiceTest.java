package es.upm.park.service;

import es.upm.park.model.GroupTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static es.upm.park.service.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupServiceTest {

    @TempDir
    Path dir;

    private Park park;
    private GroupService groups;
    private TicketService tickets;

    @BeforeEach
    void setUp() throws OperationException {
        park = ParkFixture.openWithUsers(dir);
        groups = new GroupService(park);
        tickets = new TicketService(park);
        groups.create("G01", "Familia Garcia");
    }

    @Test
    void createsEmptyGroup() {
        assertEquals(0, park.findGroup("G01").size());
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
    }

    @Test
    void rejectsDuplicateOrBlankGroup() {
        assertThrows(OperationException.class, () -> groups.create("G01", "Otro"));
        assertThrows(OperationException.class, () -> groups.create("", "Otro"));
        assertThrows(OperationException.class, () -> groups.create("G09", " "));
    }

    @Test
    void addsAndRemovesMembers() throws OperationException {
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(2, park.findGroup("G01").size());
        groups.removeMember("G01", "ana@correo.com");
        assertFalse(park.findGroup("G01").contains(ParkFixture.user(park, "ana@correo.com")));
    }

    @Test
    void rejectsDuplicateMemberAndUnknowns() throws OperationException {
        groups.addMember("G01", "ana@correo.com");
        assertThrows(OperationException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(OperationException.class, () -> groups.addMember("G01", "nobody@correo.com"));
        assertThrows(OperationException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertThrows(OperationException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertEquals(1, park.findGroup("G01").size());
    }

    @Test
    void activeGroupTicketLocksGroup() throws OperationException {
        groups.addMember("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);

        assertTrue(park.isLocked(park.findGroup("G01")));
        assertThrows(OperationException.class, () -> groups.addMember("G01", "laura@correo.com"));
        assertThrows(OperationException.class, () -> groups.removeMember("G01", "ana@correo.com"));
        assertThrows(OperationException.class, () -> groups.delete("G01"));
    }

    @Test
    void groupUnlocksWhenTicketBecomesHistorical() throws OperationException {
        groups.addMember("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));

        assertFalse(park.isLocked(park.findGroup("G01")));
        groups.addMember("G01", "laura@correo.com");
        assertEquals(2, park.findGroup("G01").size());
    }

    @Test
    void groupUnlocksWhenFutureTicketDeleted() throws OperationException {
        groups.create("G02", "Visita Mayo");
        groups.addMember("G02", "pedro@correo.com");
        groups.addMember("G02", "laura@correo.com");
        tickets.createGroupTicket("TG02", "G02", TODAY.plusDays(5));
        assertThrows(OperationException.class, () -> groups.removeMember("G02", "laura@correo.com"));

        tickets.deleteGroupTicket("TG02");
        groups.removeMember("G02", "laura@correo.com");
        groups.delete("G02");
        assertNull(park.findGroup("G02"));
    }

    @Test
    void deletingGroupKeepsHistoricalTicketsWithoutGroup() throws OperationException {
        groups.addMember("G01", "ana@correo.com");
        GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));

        groups.delete("G01");

        assertNull(park.findGroup("G01"));
        assertNull(ticket.getGroup());
        assertEquals(ticket, park.findTicket("TG01"));
        assertEquals(1, ticket.getComposition().size());
    }

    @Test
    void deletingUnknownGroupFails() {
        assertThrows(OperationException.class, () -> groups.delete("G99"));
    }
}
