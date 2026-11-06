package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerGroupTicketTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";
    private static final String PEDRO = "pedro@correo.com";
    private static final String LUIS = "luis@correo.com";

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.withDnis(Map.of(ANA, "12345678Z", PEDRO, "87654321X", LUIS, "11111111H")));
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", ANA);
        park.addUserToGroup("G01", PEDRO);
    }

    @Test
    void createsComposedTicketsWithDerivedIdentifiers() {
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertSame(park.findGroup("G01").orElseThrow(), ticket.getGroup());
        assertEquals(ANA, park.ticketsOf(ANA).get(0).getOwnerEmail());
        assertTrue(park.findTicket("TG01:12345678Z").isPresent());
        assertTrue(park.findTicket("TG01:87654321X").isPresent());
    }

    @Test
    void rejectsEmptyUnknownGroupAndPastDate() {
        park.createGroup("G02", "Vacio");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG02", "G02", TODAY));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG02", "G99", TODAY));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG02", "G01", TODAY.minusDays(1)));
        assertTrue(park.findTicket("TG02").isEmpty());
    }

    @Test
    void identifierConflictCreatesNothing() {
        park.createIndividualTicket("TG01:87654321X", PEDRO, TODAY);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", TODAY));
        assertTrue(park.findTicket("TG01").isEmpty());
        assertTrue(park.findTicket("TG01:12345678Z").isEmpty());
        park.createIndividualTicket("TG05", ANA, TODAY);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG05", "G01", TODAY));
    }

    @Test
    void compositionDoesNotFollowLaterGroupChanges() {
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.addUserToGroup("G01", LUIS);
        assertEquals(2, ticket.getComposition().size());
    }

    @Test
    void deletesFutureGroupTicketWithCompositionAndUnlocksGroup() {
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(5));
        assertTrue(park.isGroupLocked("G01"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG01:12345678Z"));
        park.deleteGroupTicket("TG01");
        assertTrue(park.findTicket("TG01").isEmpty());
        assertTrue(park.findTicket("TG01:12345678Z").isEmpty());
        assertFalse(park.isGroupLocked("G01"));
        park.addUserToGroup("G01", LUIS);
    }

    @Test
    void todaysGroupTicketIsKept() {
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("T999"));
        assertTrue(park.findTicket("TG01:87654321X").isPresent());
    }

    @Test
    void deletingGroupKeepsHistoricalGroupTicketsWithoutReference() {
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteGroup("G01");
        Ticket kept = park.findTicket("TG01").orElseThrow();
        assertSame(ticket, kept);
        assertNull(ticket.getGroup());
        assertEquals(new BigDecimal("80.00"), kept.getPrice());
        assertEquals(2, ticket.getComposition().size());
    }

    @Test
    void activeComposedTicketBlocksUserDeletionEvenOutsideGroups() {
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.deleteUserReferences(ANA));
        park.setCurrentDate(TODAY.plusDays(1));
        park.removeUserFromGroup("G01", ANA);
        park.setCurrentDate(TODAY);
        assertThrows(ParkException.class, () -> park.deleteUserReferences(ANA), "active composed ticket");
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteUserReferences(ANA);
    }
}
