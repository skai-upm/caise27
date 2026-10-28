package es.upm.etsisi.amusementpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerGroupTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.addUser(new User("DNI-ana@correo.com", "ana@correo.com"));
        controller.addUser(new User("DNI-pedro@correo.com", "pedro@correo.com"));
    }

    @Test
    void createAndPopulateGroup() {
        Group group = controller.createGroup("G01", "Familia Garcia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.addUserToGroup("G01", "pedro@correo.com");
        assertEquals(2, group.getMembers().size());
        assertTrue(controller.isGroupModifiable(group));
    }

    @Test
    void duplicateGroupRejected() {
        controller.createGroup("G01", "Familia Garcia");
        assertThrows(IllegalArgumentException.class, () -> controller.createGroup("G01", "Otro"));
        assertEquals("Familia Garcia", controller.findGroup("G01").orElseThrow().getName());
    }

    @Test
    void unknownUserOrGroupRejected() {
        controller.createGroup("G01", "Familia Garcia");
        assertThrows(IllegalArgumentException.class, () -> controller.addUserToGroup("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> controller.addUserToGroup("G99", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> controller.removeUserFromGroup("G99", "ana@correo.com"));
    }

    @Test
    void duplicateUserRejected() {
        assertThrows(IllegalArgumentException.class, () -> controller.addUser(new User("DNI-ana@correo.com", "ana@correo.com")));
    }
}
