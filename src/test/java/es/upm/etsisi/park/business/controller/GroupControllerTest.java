package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupServiceTest {

    @TempDir
    Path dir;

    private Park park;
    private GroupController groups;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        groups = park.getGroupController();
        groups.create("G01", "Familia Garcia");
    }

    private Group g01() {
        return park.getRepositories().groups().findById("G01").orElseThrow();
    }

    @Test
    void createsEmptyGroupAndRejectsDuplicates() {
        assertTrue(g01().isEmpty());
        assertThrows(ParkException.class, () -> groups.create("G01", "Otro"));
        assertThrows(ParkException.class, () -> groups.create("", "Otro"));
        assertThrows(ParkException.class, () -> groups.create("G09", " "));
    }

    @Test
    void addsAndRemovesMembers() {
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(2, g01().getMembers().size());

        groups.removeMember("G01", "ana@correo.com");
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void rejectsDuplicateUnknownAndNonMembers() {
        groups.addMember("G01", "ana@correo.com");

        assertThrows(ParkException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> groups.addMember("G01", "nobody@correo.com"));
        assertThrows(ParkException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void activeGroupTicketLocksGroupUntilItBecomesHistorical() {
        groups.addMember("G01", "ana@correo.com");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");

        assertThrows(ParkException.class, () -> groups.addMember("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> groups.removeMember("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> groups.delete("G01"));

        park.getSystemController().setDate("2027-04-16");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(2, g01().getMembers().size());
    }

    @Test
    void deletingFutureTicketUnlocksGroup() {
        groups.addMember("G01", "ana@correo.com");
        park.getTicketController().createGroup("TG02", "G01", "2027-04-20");
        assertThrows(ParkException.class, () -> groups.removeMember("G01", "ana@correo.com"));

        park.getTicketController().deleteGroup("TG02");
        groups.removeMember("G01", "ana@correo.com");
        groups.delete("G01");

        assertTrue(park.getRepositories().groups().findById("G01").isEmpty());
    }

    @Test
    void deletingGroupKeepsHistoricalTicketsWithoutGroup() {
        groups.addMember("G01", "ana@correo.com");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
        park.getSystemController().setDate("2027-05-01");

        groups.delete("G01");

        GroupTicket historical = (GroupTicket) park.getRepositories().tickets().findById("TG01").orElseThrow();
        assertNull(historical.getGroup());
        assertEquals(1, historical.getTickets().size());
    }
}
