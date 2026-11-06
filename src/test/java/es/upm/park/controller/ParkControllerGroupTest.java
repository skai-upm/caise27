package es.upm.park.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerGroupTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";
    private static final String PEDRO = "pedro@correo.com";

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.of(ANA, PEDRO));
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
        park.addUserToGroup("G01", ANA);
        park.addUserToGroup("G01", PEDRO);
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", ANA));
        assertEquals(Set.of(ANA, PEDRO), members());
    }

    @Test
    void rejectsUnknownUserAndUnknownGroup() {
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G99", ANA));
        assertTrue(members().isEmpty());
    }

    @Test
    void removesOnlyMembers() {
        park.addUserToGroup("G01", ANA);
        assertThrows(ParkException.class, () -> park.removeUserFromGroup("G01", PEDRO));
        park.removeUserFromGroup("G01", ANA);
        assertTrue(members().isEmpty());
    }

    @Test
    void lockIsDerivedFromGroupTicketsAndSystemDate() {
        park.addUserToGroup("G01", ANA);
        assertFalse(park.isGroupLocked("G01"));
        park.createGroupTicket("TG01", "G01", TODAY);
        assertTrue(park.isGroupLocked("G01"), "a group ticket for today locks");
        park.setCurrentDate(TODAY.plusDays(1));
        assertFalse(park.isGroupLocked("G01"), "historical group tickets do not lock");
    }

    @Test
    void activeGroupTicketBlocksMembershipChangesWithoutChangingState() {
        park.addUserToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(3));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", PEDRO));
        assertThrows(ParkException.class, () -> park.removeUserFromGroup("G01", ANA));
        assertEquals(Set.of(ANA), members());
    }

    @Test
    void historicalGroupTicketsAllowMembershipChanges() {
        park.addUserToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.addUserToGroup("G01", PEDRO);
        assertEquals(Set.of(ANA, PEDRO), members());
    }

    @Test
    void deleteRespectsGroupTicketsForTodayOrLater() {
        park.addUserToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertTrue(park.findGroup("G01").isPresent());
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteGroup("G01");
        assertTrue(park.findGroup("G01").isEmpty());
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
    }
}
