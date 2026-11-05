package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupManagementTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private ParkController park;
    private final List<LocalDate> ticketDates = new ArrayList<>();

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(email -> Set.of("ana@correo.com", "pedro@correo.com").contains(email));
        ticketDates.clear();
        park.setGroupTicketDates(id -> "G01".equals(id) ? ticketDates : List.of());
        park.createGroup("G01", "Familia Garcia");
    }

    @Test
    void createdGroupIsEmpty() {
        Group group = park.findGroup("G01");
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.getMembers().isEmpty());
    }

    @Test
    void duplicateIdentifierRejected() {
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otros"));
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
    }

    @Test
    void blankFieldsRejected() {
        assertThrows(ParkException.class, () -> park.createGroup("", "Name"));
        assertThrows(ParkException.class, () -> park.createGroup("G02", " "));
        assertNull(park.findGroup("G02"));
    }

    @Test
    void addUsersProgressivelyWithoutDuplicates() {
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "ana@correo.com"));
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), List.copyOf(park.findGroup("G01").getMembers()));
    }

    @Test
    void unknownUserOrGroupRejected() {
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "nobody@correo.com"));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G99", "ana@correo.com"));
        assertTrue(park.findGroup("G01").getMembers().isEmpty());
    }

    @Test
    void membersViewIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> park.findGroup("G01").getMembers().add("x"));
    }

    @Test
    void removeMemberAndRejectNonMember() {
        park.addUserToGroup("G01", "ana@correo.com");
        park.removeUserFromGroup("G01", "ana@correo.com");
        assertFalse(park.findGroup("G01").hasMember("ana@correo.com"));
        assertThrows(ParkException.class, () -> park.removeUserFromGroup("G01", "pedro@correo.com"));
    }

    @Test
    void deleteGroupWithOnlyPastTickets() {
        ticketDates.add(TODAY.minusDays(1));
        assertFalse(park.isGroupLocked("G01"));
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
    }

    @Test
    void ticketTodayLocksDeletion() {
        ticketDates.add(TODAY);
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertNotNull(park.findGroup("G01"));
    }

    @Test
    void deleteMissingGroupRejected() {
        assertThrows(ParkException.class, () -> park.deleteGroup("G99"));
    }

    @Test
    void futureTicketLocksMembershipChanges() {
        park.addUserToGroup("G01", "ana@correo.com");
        ticketDates.add(TODAY.plusDays(10));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.removeUserFromGroup("G01", "ana@correo.com"));
        assertEquals(Set.of("ana@correo.com"), park.findGroup("G01").getMembers());
    }

    @Test
    void historicalTicketsDoNotLock() {
        ticketDates.add(TODAY.minusDays(3));
        park.addUserToGroup("G01", "ana@correo.com");
        assertTrue(park.findGroup("G01").hasMember("ana@correo.com"));
    }

    @Test
    void lockReevaluatedWhenDateChangesOrTicketRemoved() {
        ticketDates.add(TODAY);
        assertTrue(park.isGroupLocked("G01"));
        park.setCurrentDate(TODAY.plusDays(1));
        assertFalse(park.isGroupLocked("G01"));
        park.setCurrentDate(TODAY);
        ticketDates.clear();
        assertFalse(park.isGroupLocked("G01"));
    }
}
