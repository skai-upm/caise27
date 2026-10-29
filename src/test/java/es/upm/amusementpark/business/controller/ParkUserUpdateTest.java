package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.amusementpark.business.exception.AuthenticationException;
import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * User creation and modification rules (Delivery 1), through the park facade.
 */
class ParkUserUpdateTest extends ParkTestSupport {

    @BeforeEach
    void users() throws Exception {
        createAna();
    }

    @Test
    void createRejectsInvalidData() {
        assertInstanceOf(ValidationException.class, assertThrows(ParkException.class, () -> park.createUser("P",
                "L", "87654321A", "pedro@correo.com", "Otra2#", "6", "9876543210987654")));
        assertThrows(ValidationException.class, () -> park.createUser("P", "L", "87654321X",
                "pedro@correo.es", "Otra2#", "6", "9876543210987654"));
        assertThrows(ValidationException.class, () -> park.createUser("P", "L", "87654321X",
                "pedro@correo.com", "otra2#", "6", "9876543210987654"));
        assertThrows(ValidationException.class, () -> park.createUser("P", "L", "87654321X",
                "pedro@correo.com", "Otra2#", "6", "98765432109876"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void createRejectsDuplicates() {
        assertThrows(DuplicateException.class, () -> park.createUser("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(DuplicateException.class, () -> park.createUser("Otra", "Persona", "87654321X",
                "ana@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void updateEachSimpleField() throws Exception {
        park.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        park.updateUser("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        park.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        park.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        restart();

        User ana = park.findUser("ana@correo.com");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updateEmailRekeysUser() throws Exception {
        park.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertNull(park.findUser("ana@correo.com"));
        assertNotNull(park.findUser("ana.nueva@correo.com"));
        park.updateUser("ana.nueva@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
    }

    @Test
    void updateRejectsWrongCredentials() {
        assertThrows(NotFoundException.class,
                () -> park.updateUser("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertThrows(AuthenticationException.class,
                () -> park.updateUser("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertEquals("Ana", park.findUser("ana@correo.com").getFirstName());
    }

    @Test
    void updateRejectsInvalidValues() {
        String hash = park.findUser("ana@correo.com").getPasswordHash();
        assertThrows(ValidationException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(ValidationException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave"));
        assertThrows(ValidationException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        User ana = park.findUser("ana@correo.com");
        assertEquals(hash, ana.getPasswordHash());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() throws Exception {
        createPedro();
        assertThrows(DuplicateException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertNotNull(park.findUser("ana@correo.com"));
        assertEquals("Pedro", park.findUser("pedro@correo.com").getFirstName());
    }
}
