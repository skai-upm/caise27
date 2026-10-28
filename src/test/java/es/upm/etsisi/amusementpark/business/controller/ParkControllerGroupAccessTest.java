package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerGroupAccessTest {

    private final ParkController controller = ParkController.getInstance();
    private final User ana = new User("1A", "ana@correo.com");
    private final User pedro = new User("2B", "pedro@correo.com");
    private final User luis = new User("3C", "luis@correo.com");
    private final User maria = new User("4D", "maria@correo.com");

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-15");
        for (User user : new User[] {ana, pedro, luis, maria}) {
            controller.addUser(user);
        }
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.addUserToGroup("G01", "pedro@correo.com");
        controller.addUserToGroup("G01", "luis@correo.com");
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        controller.createGroupTicket("TG02", "G01", "2027-04-16");
        controller.createIndividualTicket("T001", "maria@correo.com", "2027-04-15");
        controller.createIndividualTicket("T002", "ana@correo.com", "2027-04-15");
    }

    @Test
    void wholeGroupEnters() {
        Attraction a = controller.createAttraction("A01", "Dragon Rojo", "3");
        assertEquals(3, controller.enterAttraction("A01", "TG01"));
        assertTrue(a.isInside(ana) && a.isInside(pedro) && a.isInside(luis));
    }

    @Test
    void insufficientCapacityNobodyEnters() {
        Attraction a = controller.createAttraction("A01", "Dragon Rojo", "3");
        controller.enterAttraction("A01", "T001");
        assertThrows(ParkException.class, () -> controller.enterAttraction("A01", "TG01"));
        assertEquals(1, a.getOccupants().size());
    }

    @Test
    void wrongDateOrMemberInsideNobodyEnters() {
        Attraction a = controller.createAttraction("A01", "Dragon Rojo", "10");
        assertThrows(ParkException.class, () -> controller.enterAttraction("A01", "TG02"));
        controller.enterAttraction("A01", "T002");
        assertThrows(ParkException.class, () -> controller.enterAttraction("A01", "TG01"));
        assertEquals(1, a.getOccupants().size());
    }

    @Test
    void exitReleasesCapacityAndMembersLeaveIndividually() {
        Attraction a = controller.createAttraction("A01", "Dragon Rojo", "3");
        controller.enterAttraction("A01", "TG01");
        controller.exitAttraction("A01", "pedro@correo.com");
        assertFalse(a.isInside(pedro));
        assertTrue(a.isInside(ana) && a.isInside(luis));
        assertEquals(1, a.getAvailablePlaces());
        controller.enterAttraction("A01", "T001");
        assertEquals(0, a.getAvailablePlaces());
    }

    @Test
    void exitRejections() {
        controller.createAttraction("A01", "Dragon Rojo", "3");
        assertThrows(ParkException.class, () -> controller.exitAttraction("A01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> controller.exitAttraction("A99", "ana@correo.com"));
    }
}
