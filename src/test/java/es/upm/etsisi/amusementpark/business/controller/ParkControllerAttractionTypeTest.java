package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerAttractionTypeTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-15");
        controller.addUser(new User("1A", "ana@correo.com"));
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
    }

    @Test
    void typedCreation() {
        assertEquals(AttractionType.GENERAL, controller.createAttraction("A01", "Dragon", "20").getType());
        assertEquals(AttractionType.GROUP, controller.createAttraction("A03", "Rafting", "24", "GROUP").getType());
        assertThrows(InvalidValueException.class, () -> controller.createAttraction("A05", "Noria", "5", "VIP"));
        assertTrue(controller.findAttraction("A05").isEmpty());
    }

    @Test
    void entryRestrictedByType() {
        controller.createAttraction("A02", "Individual", "10", "INDIVIDUAL");
        controller.createAttraction("A03", "Rafting", "10", "GROUP");
        controller.createAttraction("A04", "Terror", "10", "SPECIAL");
        assertThrows(OperationNotAllowedException.class, () -> controller.enterAttraction("A02", "TG01"));
        assertThrows(OperationNotAllowedException.class, () -> controller.enterAttraction("A03", "T001"));
        assertThrows(OperationNotAllowedException.class, () -> controller.enterAttraction("A04", "T001"));
        assertEquals(1, controller.enterAttraction("A02", "T001"));
        assertEquals(1, controller.enterAttraction("A03", "TG01"));
        controller.buySpecialPass("PE01", "T001");
        assertEquals(1, controller.enterAttraction("A04", "T001"));
    }
}
