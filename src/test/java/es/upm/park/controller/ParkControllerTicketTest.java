package es.upm.park.controller;

import es.upm.park.model.IndividualTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerTicketTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";
    private static final String PEDRO = "pedro@correo.com";

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.of(ANA, PEDRO));
    }

    @Test
    void createsTicketAtCurrentBasePrice() {
        IndividualTicket ticket = park.createIndividualTicket("T001", ANA, TODAY);
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        park.setBasePrice(new BigDecimal("45.50"));
        assertEquals(new BigDecimal("45.50"), park.createIndividualTicket("T002", ANA, TODAY.plusDays(1)).getPrice());
        park.setBasePrice(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("40.00"), park.findTicket("T001").orElseThrow().getPrice());
    }

    @Test
    void rejectsInvalidCreationsWithoutCreatingTickets() {
        park.createIndividualTicket("T001", ANA, TODAY);
        assertThrows(ParkException.class, () -> park.createIndividualTicket("T001", PEDRO, TODAY));
        assertThrows(ParkException.class, () -> park.createIndividualTicket("T002", "nadie@correo.com", TODAY));
        assertThrows(ParkException.class, () -> park.createIndividualTicket("T003", ANA, TODAY.minusDays(1)));
        assertThrows(ParkException.class, () -> park.createIndividualTicket(" ", ANA, TODAY));
        assertEquals(ANA, ((IndividualTicket) park.findTicket("T001").orElseThrow()).getOwnerEmail());
        assertTrue(park.findTicket("T002").isEmpty());
        assertTrue(park.findTicket("T003").isEmpty());
    }

    @Test
    void ticketsOfUserAreFoundByTraversal() {
        park.createIndividualTicket("T001", ANA, TODAY);
        park.createIndividualTicket("T002", ANA, TODAY.plusDays(2));
        park.createIndividualTicket("T003", PEDRO, TODAY);
        assertEquals(List.of("T001", "T002"), park.ticketsOf(ANA).stream().map(IndividualTicket::getId).toList());
    }

    @Test
    void deletesOnlyStrictlyFutureTickets() {
        park.createIndividualTicket("T001", ANA, TODAY);
        park.createIndividualTicket("T002", ANA, TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertTrue(park.findTicket("T001").isPresent());
        park.deleteTicket("T002");
        assertTrue(park.findTicket("T002").isEmpty());
        assertThrows(ParkException.class, () -> park.deleteTicket("T999"));
        park.setCurrentDate(TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"), "past tickets are historical");
    }

    @Test
    void activeTicketBlocksUserDeletion() {
        park.createGroup("G02", "Amigos");
        park.addUserToGroup("G02", ANA);
        park.createIndividualTicket("T001", ANA, TODAY);
        assertThrows(ParkException.class, () -> park.deleteUserReferences(ANA));
        assertTrue(park.findGroup("G02").orElseThrow().hasMember(ANA), "rejected deletion changes nothing");
    }

    @Test
    void historicalTicketsDoNotBlockAndAreKept() {
        park.createIndividualTicket("T001", ANA, TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteUserReferences(ANA);
        assertTrue(park.findTicket("T001").isPresent());
    }

    @Test
    void lockedGroupBlocksUserDeletion() {
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", ANA);
        park.addUserToGroup("G01", PEDRO);
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.deleteUserReferences(PEDRO));
        assertTrue(park.findGroup("G01").orElseThrow().hasMember(PEDRO));
    }

    @Test
    void allowedDeletionRemovesUserFromGroups() {
        park.createGroup("G01", "Familia");
        park.createGroup("G02", "Amigos");
        park.addUserToGroup("G01", ANA);
        park.addUserToGroup("G02", ANA);
        park.addUserToGroup("G02", PEDRO);
        park.deleteUserReferences(ANA);
        assertFalse(park.findGroup("G01").orElseThrow().hasMember(ANA));
        assertFalse(park.findGroup("G02").orElseThrow().hasMember(ANA));
        assertTrue(park.findGroup("G02").orElseThrow().hasMember(PEDRO));
    }
}
