package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.text.TextUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserControllerTest {

    @TempDir
    Path dir;

    private Path file;
    private static final BCryptPasswordHasher HASHER = new BCryptPasswordHasher(4);

    private UserController service;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        service = load();
    }

    private UserController load() {
        UserController controller = UserController.getInstance();
        controller.configure(new TextUserRepository(file), HASHER);
        return controller;
    }

    private void createAna() {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    @Test
    void createsAndPersistsUser() throws Exception {
        createAna();
        assertEquals(1, service.getUsers().size());
        String line = Files.readAllLines(file).get(0);
        assertTrue(line.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"), line);
        assertFalse(line.contains("Clave1!"));
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(ParkException.class, () -> service.create("Ana", "G", "12345678A", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> service.create("Ana", "G", "12345678Z", "ana@correo.es", "Clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> service.create("Ana", "G", "12345678Z", "ana@correo.com", "clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> service.create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "123456789012345"));
        assertThrows(ParkException.class, () -> service.create("", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(0, service.getUsers().size());
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        createAna();
        assertThrows(ParkException.class, () -> service.create("Otra", "X", "12345678Z", "otra@correo.com", "Clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> service.create("Otra", "X", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void reloadedUsersKeepUniqueness() {
        createAna();
        UserController reloaded = load();
        assertNotNull(reloaded.findByEmail("ana@correo.com"));
        assertThrows(ParkException.class, () -> reloaded.create("Otra", "X", "12345678Z", "otra@correo.com", "Clave1!", "6", "1234567890123456"));
    }

    @Test
    void deletesWithCorrectPassword() throws Exception {
        createAna();
        createPedro();
        service.remove(service.authenticate("pedro@correo.com", "Otra2#"));
        assertNull(service.findByEmail("pedro@correo.com"));
        assertEquals(1, Files.readAllLines(file).size());
    }

    @Test
    void deleteRejectsWrongPasswordOrUnknownEmail() {
        createAna();
        assertThrows(ParkException.class, () -> service.authenticate("ana@correo.com", "Wrong1!"));
        assertThrows(ParkException.class, () -> service.authenticate("nadie@correo.com", "Clave1!"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void updatesEveryModifiableField() {
        createAna();
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");

        User reloaded = load().findByEmail("ana.nueva@correo.com");
        assertNotNull(reloaded);
        assertEquals("Ana Maria", reloaded.getFirstName());
        assertEquals("Garcia", reloaded.getSurnames());
        assertEquals("699999999", reloaded.getPhone());
        assertEquals("9999888877776666", reloaded.getCard());
        assertTrue(HASHER.matches("Nueva2#", reloaded.getPasswordHash()));
        assertFalse(HASHER.matches("Clave1!", reloaded.getPasswordHash()));
        assertFalse(reloaded.getPasswordHash().contains("Nueva2#"));
        assertEquals("12345678Z", reloaded.getDni());
    }

    @Test
    void updateRejectsInvalidValuesAndKeepsState() {
        createAna();
        createPedro();
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es"));
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", "Clave1!", "CARD", "123456789012345"));
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", "Bad1!", "NAME", "X"));
        assertThrows(ParkException.class, () -> service.update("x@correo.com", "Clave1!", "NAME", "X"));
        User ana = service.findByEmail("ana@correo.com");
        assertTrue(HASHER.matches("Clave1!", ana.getPasswordHash()));
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Ana", ana.getFirstName());
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() {
        createAna();
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }
}
