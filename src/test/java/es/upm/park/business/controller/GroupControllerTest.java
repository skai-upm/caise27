package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.GroupTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupControllerTest {

    @TempDir
    Path dir;

    private Park park;
    private GroupController groups;
    private TicketController tickets;

    @BeforeEach
    void setUp() throws ParkException {
        park = ParkFixture.openWithUsers(dir);
        groups = GroupController.getInstance();
        tickets = TicketController.getInstance();
        groups.create("G01", "Familia Garcia");
    }

    @Test
    void createsEmptyGroup() {
        assertEquals(0, park.findGroup("G01").size());
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
    }

    @Test
    void rejectsDuplicateOrBlankGroup() {
        assertThrows(ParkException.class, () -> groups.create("G01", "Otro"));
        assertThrows(ParkException.class, () -> groups.create("", "Otro"));
        assertThrows(ParkException.class, () -> groups.create("G09", " "));
    }

    @Test
    void addsAndRemovesMembers() throws ParkException {
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(2, park.findGroup("G01").size());
        groups.removeMember("G01", "ana@correo.com");
        assertFalse(park.findGroup("G01").contains(ParkFixture.user(park, "ana@correo.com")));
    }

    @Test
    void rejectsDuplicateMemberAndUnknowns() throws ParkException {
        groups.addMember("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> groups.addMember("G01", "nobody@correo.com"));
        assertThrows(ParkException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertEquals(1, park.findGroup("G01").size());
    }

    @Test
    void activeGroupTicketLocksGroup() throws ParkException {
        groups.addMember("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);

        assertTrue(park.isLocked(park.findGroup("G01")));
        assertThrows(ParkException.class, () -> groups.addMember("G01", "laura@correo.com"));
        assertThrows(ParkException.class, () -> groups.removeMember("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> groups.delete("G01"));
    }

    @Test
    void groupUnlocksWhenTicketBecomesHistorical() throws ParkException {
        groups.addMember("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));

        assertFalse(park.isLocked(park.findGroup("G01")));
        groups.addMember("G01", "laura@correo.com");
        assertEquals(2, park.findGroup("G01").size());
    }

    @Test
    void groupUnlocksWhenFutureTicketDeleted() throws ParkException {
        groups.create("G02", "Visita Mayo");
        groups.addMember("G02", "pedro@correo.com");
        groups.addMember("G02", "laura@correo.com");
        tickets.createGroupTicket("TG02", "G02", TODAY.plusDays(5));
        assertThrows(ParkException.class, () -> groups.removeMember("G02", "laura@correo.com"));

        tickets.deleteGroupTicket("TG02");
        groups.removeMember("G02", "laura@correo.com");
        groups.delete("G02");
        assertNull(park.findGroup("G02"));
    }

    @Test
    void deletingGroupKeepsHistoricalTicketsWithoutGroup() throws ParkException {
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
        assertThrows(ParkException.class, () -> groups.delete("G99"));
    }
}
