package es.upm.park.business.controller;

import es.upm.park.business.exception.AuthenticationException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest {

    @TempDir
    Path dir;

    private Park park;
    private UserController users;

    @BeforeEach
    void setUp() {
        park = ParkFixture.openEmpty(dir);
        users = UserController.getInstance();
    }

    private void createAna() throws ParkException {
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() throws ParkException {
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void createsAndPersistsUser() throws Exception {
        createAna();
        assertEquals(1, park.getUsers().size());
        assertNotNull(ParkFixture.restart(dir).findUserByEmail("ana@correo.com"));
    }

    @Test
    void rejectsInvalidDataWithValidationExceptions() {
        assertThrows(ValidationException.class, () -> users.create("Ana", "Garcia", "12345678A",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(ValidationException.class, () -> users.create("Ana", "Garcia", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(ValidationException.class, () -> users.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(ValidationException.class, () -> users.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "123"));
        assertThrows(ValidationException.class, () -> users.create("", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(ValidationException.class, () -> users.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", " ", "1234567890123456"));
        assertTrue(park.getUsers().isEmpty());
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void rejectsDuplicateDniAndEmail() throws Exception {
        createAna();
        ConflictException e = assertThrows(ConflictException.class, () -> users.create("Otra",
                "Persona", "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertThrows(ConflictException.class, () -> users.create("Otra", "Persona", "87654321X",
                "ANA@correo.com", "Clave1!", "600000000", "1234567890123456"));
    }

    @Test
    void authenticationFailures() throws Exception {
        createAna();
        assertThrows(AuthenticationException.class, () -> users.authenticate("ana@correo.com", "Wrong1!"));
        assertThrows(NotFoundException.class, () -> users.authenticate("nobody@correo.com", "Clave1!"));
        assertEquals("ana@correo.com", users.authenticate("ana@correo.com", "Clave1!").getEmail());
    }

    @Test
    void deletesWithCorrectPassword() throws Exception {
        createAna();
        createPedro();
        users.delete("pedro@correo.com", "Otra2#");
        assertNotNull(ParkFixture.restart(dir).findUserByEmail("ana@correo.com"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void updatesEveryModifiableField() throws Exception {
        createAna();
        users.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        users.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        users.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        users.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        users.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        users.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");

        ParkFixture.restart(dir);
        User stored = park.findUserByEmail("ana.nueva@correo.com");
        assertNotNull(stored);
        assertEquals("Ana Maria", stored.getFirstName());
        assertEquals("Garcia", stored.getSurnames());
        assertEquals("699999999", stored.getPhone());
        assertEquals("9999888877776666", stored.getCard());
        assertEquals("12345678Z", stored.getDni());
        assertNotNull(users.authenticate("ana.nueva@correo.com", "Nueva2#"));
        assertThrows(AuthenticationException.class, () -> users.authenticate("ana.nueva@correo.com", "Clave1!"));
    }

    @Test
    void emailUpdateRules() throws Exception {
        createAna();
        createPedro();
        users.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertThrows(ConflictException.class,
                () -> users.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
    }

    @Test
    void rejectsInvalidNewValuesWrongCredentialsAndDni() throws Exception {
        createAna();
        assertThrows(ValidationException.class, () -> users.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(ValidationException.class,
                () -> users.update("ana@correo.com", "Clave1!", "PASSWORD", "weak"));
        assertThrows(AuthenticationException.class,
                () -> users.update("ana@correo.com", "Wrong1!", "PHONE", "699999999"));
        ValidationException e = assertThrows(ValidationException.class,
                () -> users.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertTrue(e.getMessage().contains("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
        User ana = park.findUserByEmail("ana@correo.com");
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("612345678", ana.getPhone());
    }
}
