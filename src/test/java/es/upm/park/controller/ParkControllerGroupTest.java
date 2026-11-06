package es.upm.park.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerGroupTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private final List<LocalDate> ticketDates = new ArrayList<>();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        Set<String> users = Set.of("ana@correo.com", "pedro@correo.com");
        park.setUserExistsLookup(users::contains);
        park.setGroupTicketDatesLookup(id -> id.equals("G01") ? ticketDates : List.of());
        park.createGroup("G01", "Familia Garcia");
    }

    private Set<String> members() {
        return park.findGroup("G01").orElseThrow().getMemberEmails();
    }

    @Test
    void createRejectsDuplicateAndMissingFields() {
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otro"));
        assertEquals("Familia Garcia", park.findGroup("G01").orElseThrow().getName());
        assertThrows(ParkException.class, () -> park.createGroup("", "Nombre"));
        assertThrows(ParkException.class, () -> park.createGroup("G02", " "));
        assertTrue(park.findGroup("G02").isEmpty());
    }

    @Test
    void addsRegisteredUsersOnlyOnce() {
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "ana@correo.com"));
        assertEquals(Set.of("ana@correo.com", "pedro@correo.com"), members());
    }

    @Test
    void rejectsUnknownUserAndUnknownGroup() {
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G99", "ana@correo.com"));
        assertTrue(members().isEmpty());
    }

    @Test
    void removesOnlyMembers() {
        park.addUserToGroup("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.removeUserFromGroup("G01", "pedro@correo.com"));
        park.removeUserFromGroup("G01", "ana@correo.com");
        assertTrue(members().isEmpty());
    }

    @Test
    void lockIsDerivedFromTicketDatesAndSystemDate() {
        assertFalse(park.isGroupLocked("G01"));
        ticketDates.add(TODAY.minusDays(1));
        assertFalse(park.isGroupLocked("G01"), "historical tickets do not lock");
        ticketDates.add(TODAY);
        assertTrue(park.isGroupLocked("G01"), "a ticket for today locks");
        park.setCurrentDate(TODAY.plusDays(1));
        assertFalse(park.isGroupLocked("G01"), "lock follows the system date");
    }

    @Test
    void activeTicketBlocksMembershipChangesWithoutChangingState() {
        park.addUserToGroup("G01", "ana@correo.com");
        ticketDates.add(TODAY.plusDays(3));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.removeUserFromGroup("G01", "ana@correo.com"));
        assertEquals(Set.of("ana@correo.com"), members());
    }

    @Test
    void historicalTicketsAllowMembershipChanges() {
        ticketDates.add(TODAY.minusDays(10));
        park.addUserToGroup("G01", "pedro@correo.com");
        assertEquals(Set.of("pedro@correo.com"), members());
    }

    @Test
    void deleteRespectsTicketsForTodayOrLater() {
        ticketDates.add(TODAY);
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertTrue(park.findGroup("G01").isPresent());
        ticketDates.clear();
        ticketDates.add(TODAY.minusDays(1));
        park.deleteGroup("G01");
        assertTrue(park.findGroup("G01").isEmpty());
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
    }
}
