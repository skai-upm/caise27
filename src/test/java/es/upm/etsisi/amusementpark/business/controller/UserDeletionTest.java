package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDeletionTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-15");
        controller.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void correctPasswordDeletes() {
        controller.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(controller.findUser("ana@correo.com").isEmpty());
    }

    @Test
    void wrongPasswordOrUnknownUserRejected() {
        assertThrows(AuthenticationException.class, () -> controller.deleteUser("ana@correo.com", "Clave1?"));
        assertThrows(NotFoundException.class, () -> controller.deleteUser("nadie@correo.com", "Clave1!"));
        assertTrue(controller.findUser("ana@correo.com").isPresent());
    }

    @Test
    void activeTicketStillBlocks() {
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(OperationNotAllowedException.class, () -> controller.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(controller.findUser("ana@correo.com").isPresent());
    }
}
