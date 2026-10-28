package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.AlreadyExistsException;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerPassTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-01");
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.createIndividualTicket("T000", "ana@correo.com", "2027-04-01");
    }

    @Test
    void speedPassPurchases() {
        List<SpeedPass> single = controller.buySpeedPasses("SP01", "T001", "1");
        assertEquals("SP01:1", single.get(0).getIdentifier());
        List<SpeedPass> pack = controller.buySpeedPasses("SP02", "T001", "10");
        assertEquals(10, pack.size());
        assertEquals("SP02:10", pack.get(9).getIdentifier());
        controller.setCurrentPrice("50");
        assertEquals(new BigDecimal("8.00"), pack.get(0).getPrice());
        assertEquals(11, controller.findPassesOf((es.upm.etsisi.amusementpark.business.model.IndividualTicket)
                controller.findTicket("T001").orElseThrow()).size());
    }

    @Test
    void speedPassRejectionsCreateNothing() {
        controller.buySpeedPasses("SP03", "T001", "1");
        controller.buySpecialPass("SP04:5", "T001");
        assertThrows(AlreadyExistsException.class, () -> controller.buySpeedPasses("SP04", "T001", "10"));
        assertTrue(controller.findPass("SP04:1").isEmpty());
        assertThrows(InvalidValueException.class, () -> controller.buySpeedPasses("SP05", "T001", "5"));
        assertThrows(NotFoundException.class, () -> controller.buySpeedPasses("SP06", "NOPE", "1"));
        controller.setCurrentDate("2027-04-02");
        assertThrows(OperationNotAllowedException.class, () -> controller.buySpeedPasses("SP07", "T000", "1"));
        assertEquals(2, controller.getPasses().size());
    }

    @Test
    void oneSpecialPassPerTicketEvenWhenConsumed() {
        SpecialPass pass = controller.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());
        pass.consume();
        assertThrows(OperationNotAllowedException.class, () -> controller.buySpecialPass("PE02", "T001"));
        controller.createIndividualTicket("T002", "ana@correo.com", "2027-04-16");
        assertThrows(AlreadyExistsException.class, () -> controller.buySpecialPass("PE01", "T002"));
    }

    @Test
    void passesForComposedTickets() {
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        for (int i = 0; i < 5; i++) {
            controller.addUser(new User("D" + i, "u" + i + "@correo.com"));
            controller.addUserToGroup("G01", "u" + i + "@correo.com");
        }
        controller.createGroupTicket("TG02", "G01", "2027-04-15");
        assertEquals(new BigDecimal("6.40"), controller.buySpeedPasses("SP", "TG02:12345678Z", "1").get(0).getPrice());
    }

    @Test
    void refunds() {
        controller.buySpeedPasses("SP02", "T001", "10");
        controller.refundSpeedPass("SP02:3");
        assertTrue(controller.findPass("SP02:3").isEmpty());
        assertEquals(9, controller.getPasses().size());
        controller.findPass("SP02:4").orElseThrow().consume();
        assertThrows(OperationNotAllowedException.class, () -> controller.refundSpeedPass("SP02:4"));
        assertThrows(NotFoundException.class, () -> controller.refundSpecialPass("SP02:5"));
        assertThrows(NotFoundException.class, () -> controller.refundSpeedPass("NOPE"));
        controller.buySpecialPass("PE01", "T000");
        assertThrows(OperationNotAllowedException.class, () -> controller.refundSpecialPass("PE01"));
        controller.buySpecialPass("PE02", "T001");
        controller.refundSpecialPass("PE02");
        assertTrue(controller.findPass("PE02").isEmpty());
    }

    @Test
    void ticketDeletionRemovesItsPasses() {
        controller.buySpeedPasses("SP01", "T001", "1");
        controller.buySpeedPasses("SP02", "T001", "10");
        controller.buySpecialPass("PE01", "T001");
        controller.buySpecialPass("PE00", "T000");
        assertThrows(OperationNotAllowedException.class, () -> controller.deleteIndividualTicket("T000"));
        assertTrue(controller.findPass("PE00").isPresent());
        controller.deleteIndividualTicket("T001");
        assertEquals(1, controller.getPasses().size());
    }

    @Test
    void groupTicketDeletionRemovesCompositionPasses() {
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        controller.createGroupTicket("TG00", "G01", "2027-04-01");
        controller.buySpeedPasses("SP", "TG01:12345678Z", "1");
        controller.buySpecialPass("PE", "TG00:12345678Z");
        assertThrows(OperationNotAllowedException.class, () -> controller.deleteGroupTicket("TG00"));
        assertTrue(controller.findPass("PE").isPresent());
        controller.deleteGroupTicket("TG01");
        assertFalse(controller.isTicketIdentifierInUse("TG01:12345678Z"));
        assertTrue(controller.findPass("SP:1").isEmpty());
        assertTrue(controller.findPass("PE").isPresent());
    }
}
