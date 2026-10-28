package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.AlreadyExistsException;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRegistrationTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
    }

    @Test
    void registerStoresAllAttributesAndProtectedPassword() {
        User ana = controller.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");
        assertEquals(ana, controller.findUser("ana@correo.com").orElseThrow());
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
        assertNotEquals("Clave1!", ana.getPasswordHash());
        controller.authenticate("ana@correo.com", "Clave1!");
    }

    @Test
    void invalidDataStoresNothing() {
        assertThrows(InvalidValueException.class, () -> controller.registerUser("Ana", "Garcia", "12345678A",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(InvalidValueException.class, () -> controller.registerUser("Ana", "Garcia", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(InvalidValueException.class, () -> controller.registerUser("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(InvalidValueException.class, () -> controller.registerUser("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234 5678 9012 3456"));
        assertTrue(controller.getUsers().isEmpty());
    }

    @Test
    void uniqueDniAndEmail() {
        controller.registerUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        assertThrows(AlreadyExistsException.class, () -> controller.registerUser("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(AlreadyExistsException.class, () -> controller.registerUser("Pedro", "Lopez", "87654321X",
                "ana@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, controller.getUsers().size());
    }
}
