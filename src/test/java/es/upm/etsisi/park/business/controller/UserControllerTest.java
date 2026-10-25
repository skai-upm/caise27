package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.TestSupport;
import es.upm.etsisi.park.business.exception.AuthenticationException;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.UserFileRepository;
import java.io.IOException;
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
    void setUp() {
        file = dir.resolve("users.txt");
        service = UserController.getInstance();
        service.initialize(new UserFileRepository(file), TestSupport.HASHER);
    }

    private void reload() {
        service.initialize(new UserFileRepository(file), TestSupport.HASHER);
    }

    private void createAna() {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void singleton() {
        assertSame(UserController.getInstance(), service);
    }

    @Test
    void createsAndPersistsUserWithHashedPassword() throws IOException {
        createAna();
        assertTrue(service.findByEmail("ana@correo.com").isPresent());
        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        String line = lines.get(0);
        assertTrue(line.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"), line);
        assertTrue(line.endsWith(";612345678;1234567890123456"), line);
        assertFalse(line.contains("Clave1!"));
    }

    @Test
    void rejectsInvalidDataOnCreate() {
        ParkException e = assertThrows(ValidationException.class, () -> service.create("Ana", "Garcia Lopez",
                "12345678A", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertThrows(ValidationException.class, () -> service.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(ValidationException.class, () -> service.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(ValidationException.class, () -> service.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "123456789012345"));
        assertThrows(ValidationException.class, () -> service.create(" ", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(service.findAll().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        createAna();
        ParkException dni = assertThrows(DuplicateException.class, () -> service.create("Otra", "Persona",
                "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(dni.getMessage().contains("DNI"));
        ParkException email = assertThrows(DuplicateException.class, () -> service.create("Otra", "Persona",
                "87654321X", "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(email.getMessage().contains("email"));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void authenticationUsesStoredHashAfterReload() {
        createAna();
        reload();
        assertEquals("12345678Z", service.authenticate("ana@correo.com", "Clave1!").getDni());
        assertThrows(AuthenticationException.class, () -> service.authenticate("ana@correo.com", "Mala1!"));
        assertThrows(NotFoundException.class, () -> service.authenticate("nadie@correo.com", "Clave1!"));
    }

    @Test
    void legacyPlainTextPasswordIsRejectedWithoutError() throws IOException {
        Files.write(file, List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        reload();
        assertThrows(AuthenticationException.class, () -> service.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void passwordChangeStoresNewHash() throws IOException {
        createAna();
        String before = Files.readAllLines(file).get(0);
        service.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        String after = Files.readAllLines(file).get(0);
        assertNotEquals(before, after);
        assertFalse(after.contains("Nueva2#"));
        reload();
        service.authenticate("ana@correo.com", "Nueva2#");
        assertThrows(AuthenticationException.class, () -> service.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void updatesEveryModifiableField() {
        createAna();
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        assertFalse(service.findByEmail("ana@correo.com").isPresent());
        User ana = service.findByEmail("ana.nueva@correo.com").orElseThrow();
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updateRules() {
        createAna();
        createPedro();
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(AuthenticationException.class, () -> service.update("ana@correo.com", "Mala1!", "PHONE", "1"));
        assertThrows(DuplicateException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave"));
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", "Clave1!", "NAME", " "));
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", "Clave1!", "PHONE", ""));
        User ana = service.findByEmail("ana@correo.com").orElseThrow();
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Ana", ana.getFirstName());
        service.authenticate("ana@correo.com", "Clave1!");
    }

    @Test
    void removeIsPersisted() {
        createAna();
        service.remove(service.authenticate("ana@correo.com", "Clave1!"));
        reload();
        assertTrue(service.findAll().isEmpty());
    }
}
