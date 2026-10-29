package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserControllerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserController service;

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        service = restart();
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserController restart() {
        UserController controller = UserController.getInstance();
        controller.configure(new TextUserRepository(file), ParkFixture.FAST_HASHER);
        return controller;
    }

    @Test
    void createStoresAndPersists() throws Exception {
        assertEquals(2, service.getUsers().size());
        assertEquals(2, Files.readAllLines(file).size());
        assertEquals(2, restart().getUsers().size());
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(ParkException.class,
                () -> service.create("Eva", "Ruiz", "11111111A", "eva@correo.com", "Clave1!", "6", "1111222233334444"));
        assertThrows(ParkException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "eva@correo.es", "Clave1!", "6", "1111222233334444"));
        assertThrows(ParkException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "eva@correo.com", "clave1!", "6", "1111222233334444"));
        assertThrows(ParkException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "eva@correo.com", "Clave1!", "6", "111122223333444"));
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniAndEmail() {
        assertThrows(ParkException.class,
                () -> service.create("Eva", "Ruiz", "12345678Z", "eva@correo.com", "Clave1!", "6", "1111222233334444"));
        assertThrows(ParkException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "ana@correo.com", "Clave1!", "6", "1111222233334444"));
        assertEquals(2, restart().getUsers().size());
    }

    @Test
    void duplicateDniDetectedAfterRestart() {
        UserController again = restart();
        assertThrows(ParkException.class,
                () -> again.create("Eva", "Ruiz", "12345678Z", "eva@correo.com", "Clave1!", "6", "1111222233334444"));
    }

    @Test
    void removeAuthenticatedUserIsPersisted() throws Exception {
        service.remove(service.authenticate("pedro@correo.com", "Otra2#"));
        assertNull(service.findByEmail("pedro@correo.com"));
        assertNull(restart().findByEmail("pedro@correo.com"));
        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        ParkFixture.assertStoredUser("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                lines.get(0));
    }

    @Test
    void authenticateRejectsWrongPasswordAndUnknownEmail() {
        assertThrows(ParkException.class, () -> service.authenticate("pedro@correo.com", "Otra2?"));
        assertThrows(ParkException.class, () -> service.authenticate("nadie@correo.com", "Otra2#"));
        assertEquals(2, restart().getUsers().size());
    }

    @Test
    void updateEachModifiableFieldIsPersisted() throws Exception {
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");

        assertNull(service.findByEmail("ana@correo.com"));
        User ana = restart().findByEmail("ana.nueva@correo.com");
        assertNotNull(ana);
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertTrue(ParkFixture.FAST_HASHER.matches("Nueva2#", ana.getPassword()));
    }

    @Test
    void updateRejectsBadCredentials() {
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Wrong1!", "PHONE", "600000000"));
        assertThrows(ParkException.class,
                () -> service.update("nadie@correo.com", "Clave1!", "PHONE", "600000000"));
        assertEquals("612345678", restart().findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void updateRejectsDniUnknownFieldAndInvalidValues() {
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "AGE", "30"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "CARD", "123456789012345"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));

        User ana = restart().findByEmail("ana@correo.com");
        assertEquals("12345678Z", ana.getDni());
        assertTrue(ParkFixture.FAST_HASHER.matches("Clave1!", ana.getPassword()));
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() throws Exception {
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }
}
