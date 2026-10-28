package es.upm.etsisi.amusementpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.User;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerTicketTest {

    private final ParkController controller = ParkController.getInstance();
    private final User ana = new User("ana@correo.com");

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-01");
        controller.addUser(ana);
    }

    @Test
    void createAtCurrentPriceAndKeepPrice() {
        IndividualTicket t1 = controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
        assertSame(ana, t1.getUser());
        controller.setCurrentPrice("45.50");
        IndividualTicket t2 = controller.createIndividualTicket("T002", "ana@correo.com", "2027-04-01");
        controller.setCurrentPrice("50");
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
        assertEquals(2, controller.findTicketsOfUser(ana).size());
    }

    @Test
    void creationRejections() {
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(IllegalArgumentException.class,
                () -> controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-16"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createIndividualTicket("T002", "nadie@correo.com", "2027-04-16"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createIndividualTicket("T003", "ana@correo.com", "2027-03-31"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createIndividualTicket("T004", "ana@correo.com", "2027-13-01"));
        assertEquals(1, controller.findTicketsOfUser(ana).size());
    }

    @Test
    void deletionRules() {
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.createIndividualTicket("T002", "ana@correo.com", "2027-04-01");
        controller.deleteIndividualTicket("T001");
        assertFalse(controller.isTicketIdentifierInUse("T001"));
        assertThrows(IllegalStateException.class, () -> controller.deleteIndividualTicket("T002"));
        controller.setCurrentDate("2027-04-02");
        assertThrows(IllegalStateException.class, () -> controller.deleteIndividualTicket("T002"));
        assertTrue(controller.isTicketIdentifierInUse("T002"));
        assertThrows(IllegalArgumentException.class, () -> controller.deleteIndividualTicket("NOPE"));
    }

    @Test
    void activeTicketBlocksUserDeletion() {
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertTrue(controller.hasActiveTickets(ana));
        assertThrows(IllegalStateException.class, () -> controller.deleteUser("ana@correo.com"));
        assertTrue(controller.findUser("ana@correo.com").isPresent());
    }

    @Test
    void historicalTicketsDoNotBlockAndAreRetained() {
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.createGroup("G01", "Familia Garcia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.setCurrentDate("2027-04-16");
        assertFalse(controller.hasActiveTickets(ana));
        controller.deleteUser("ana@correo.com");
        assertTrue(controller.findUser("ana@correo.com").isEmpty());
        assertFalse(controller.findGroup("G01").orElseThrow().hasMember("ana@correo.com"));
        IndividualTicket historical = (IndividualTicket) controller.findTicket("T001").orElseThrow();
        assertEquals("ana@correo.com", historical.getUser().getEmail());
    }

    @Test
    void deleteUnknownUser() {
        assertThrows(IllegalArgumentException.class, () -> controller.deleteUser("nadie@correo.com"));
    }
}
