package es.upm.etsisi.amusementpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.model.Attraction;
import es.upm.etsisi.amusementpark.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerAccessTest {

    private final ParkController controller = ParkController.getInstance();
    private final User ana = new User("12345678Z", "ana@correo.com");
    private final User pedro = new User("87654321X", "pedro@correo.com");
    private Attraction attraction;

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-15");
        controller.addUser(ana);
        controller.addUser(pedro);
        attraction = controller.createAttraction("A01", "Dragon Rojo", "1");
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.createIndividualTicket("T002", "pedro@correo.com", "2027-04-15");
        controller.createIndividualTicket("T003", "pedro@correo.com", "2027-04-16");
    }

    @Test
    void enterWithTicketForToday() {
        assertEquals(1, controller.enterAttraction("A01", "T001"));
        assertTrue(attraction.isInside(ana));
    }

    @Test
    void presenceSurvivesDateChange() {
        controller.enterAttraction("A01", "T001");
        controller.setCurrentDate("2027-04-16");
        assertTrue(attraction.isInside(ana));
    }

    @Test
    void rejections() {
        assertThrows(IllegalStateException.class, () -> controller.enterAttraction("A01", "T003"));
        assertThrows(IllegalArgumentException.class, () -> controller.enterAttraction("A01", "NOPE"));
        assertThrows(IllegalArgumentException.class, () -> controller.enterAttraction("A99", "T001"));
        assertTrue(attraction.getOccupants().isEmpty());
        controller.enterAttraction("A01", "T001");
        assertThrows(IllegalStateException.class, () -> controller.enterAttraction("A01", "T002"));
        assertThrows(IllegalStateException.class, () -> controller.enterAttraction("A01", "T001"));
        assertEquals(1, attraction.getOccupants().size());
    }
}
