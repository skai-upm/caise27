package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.AlreadyExistsException;
import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUpdateTest {

    private final ParkController controller = ParkController.getInstance();
    private User ana;

    @BeforeEach
    void setUp() {
        controller.reset();
        ana = controller.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        controller.registerUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void updateEveryField() {
        controller.updateUser("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        controller.updateUser("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        controller.updateUser("ana@correo.com", "Clave1!", "PHONE", "699999999");
        controller.updateUser("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        controller.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        controller.updateUser("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertSame(ana, controller.findUser("ana.nueva@correo.com").orElseThrow());
        assertTrue(controller.findUser("ana@correo.com").isEmpty());
        controller.authenticate("ana.nueva@correo.com", "Nueva2#");
        assertThrows(AuthenticationException.class, () -> controller.authenticate("ana.nueva@correo.com", "Clave1!"));
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void dniAndUnknownFieldsRejected() {
        assertThrows(InvalidValueException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(InvalidValueException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1!", "name", "Ana"));
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void credentialsChecked() {
        assertThrows(AuthenticationException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1?", "PHONE", "699999999"));
        assertThrows(NotFoundException.class,
                () -> controller.updateUser("nadie@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("612345678", ana.getPhone());
    }

    @Test
    void invalidOrDuplicateValuesKeepPreviousValue() {
        assertThrows(InvalidValueException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es"));
        assertThrows(AlreadyExistsException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        String hash = ana.getPasswordHash();
        assertThrows(InvalidValueException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(InvalidValueException.class,
                () -> controller.updateUser("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals(hash, ana.getPasswordHash());
        assertEquals("1234567890123456", ana.getCard());
        controller.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
    }

    @Test
    void groupMembershipFollowsEmailChange() {
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        assertTrue(controller.findGroup("G01").orElseThrow().hasMember("ana.nueva@correo.com"));
        controller.removeUserFromGroup("G01", "ana.nueva@correo.com");
    }
}
