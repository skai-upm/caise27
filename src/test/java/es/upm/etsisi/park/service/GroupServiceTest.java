package es.upm.etsisi.park.service;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.core.Park;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
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
    private GroupService groups;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        groups = park.getGroupService();
        groups.create("G01", "Familia Garcia");
    }

    private Group g01() {
        return park.getGroups().findById("G01").orElseThrow();
    }

    @Test
    void createsEmptyGroupAndRejectsDuplicates() {
        assertTrue(g01().isEmpty());
        assertThrows(UserOperationException.class, () -> groups.create("G01", "Otro"));
        assertThrows(UserOperationException.class, () -> groups.create("", "Otro"));
        assertThrows(UserOperationException.class, () -> groups.create("G09", " "));
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

        assertThrows(UserOperationException.class, () -> groups.addMember("G01", "ana@correo.com"));
        assertThrows(UserOperationException.class, () -> groups.addMember("G99", "ana@correo.com"));
        assertThrows(UserOperationException.class, () -> groups.addMember("G01", "nobody@correo.com"));
        assertThrows(UserOperationException.class, () -> groups.removeMember("G01", "pedro@correo.com"));
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void activeGroupTicketLocksGroupUntilItBecomesHistorical() {
        groups.addMember("G01", "ana@correo.com");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");

        assertThrows(UserOperationException.class, () -> groups.addMember("G01", "pedro@correo.com"));
        assertThrows(UserOperationException.class, () -> groups.removeMember("G01", "ana@correo.com"));
        assertThrows(UserOperationException.class, () -> groups.delete("G01"));

        park.getSystemService().setDate("2027-04-16");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(2, g01().getMembers().size());
    }

    @Test
    void deletingFutureTicketUnlocksGroup() {
        groups.addMember("G01", "ana@correo.com");
        park.getTicketService().createGroup("TG02", "G01", "2027-04-20");
        assertThrows(UserOperationException.class, () -> groups.removeMember("G01", "ana@correo.com"));

        park.getTicketService().deleteGroup("TG02");
        groups.removeMember("G01", "ana@correo.com");
        groups.delete("G01");

        assertTrue(park.getGroups().findById("G01").isEmpty());
    }

    @Test
    void deletingGroupKeepsHistoricalTicketsWithoutGroup() {
        groups.addMember("G01", "ana@correo.com");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");
        park.getSystemService().setDate("2027-05-01");

        groups.delete("G01");

        GroupTicket historical = (GroupTicket) park.getTickets().findById("TG01").orElseThrow();
        assertNull(historical.getGroup());
        assertEquals(1, historical.getTickets().size());
    }
}
