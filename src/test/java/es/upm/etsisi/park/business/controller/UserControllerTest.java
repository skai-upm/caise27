package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

import es.upm.etsisi.park.business.exception.AuthenticationException;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.business.model.user.UserField;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.support.TestContext;

class UserControllerTest {

    @TempDir
    Path dir;

    private Path file;
    private final UserController users = UserController.getInstance();

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        TestContext.initialize(dir);
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void createdUsersArePersisted() throws Exception {
        TestContext.initialize(dir);
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void storesOnlyBcryptHashes() throws Exception {
        String content = Files.readString(file);
        assertFalse(content.contains("Clave1!"));
        assertFalse(content.contains("Otra2#"));
        List<String> lines = Files.readAllLines(file);
        assertTrue(lines.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"));
        assertTrue(lines.get(0).endsWith(";612345678;1234567890123456"));
        assertNotNull(users.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void passwordChangeStoresNewHash() throws Exception {
        String oldHash = users.findByEmail("ana@correo.com").getPasswordHash();
        users.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        String newHash = users.findByEmail("ana@correo.com").getPasswordHash();
        assertNotEquals(oldHash, newHash);
        assertTrue(newHash.startsWith("$2a$"));
        TestContext.initialize(dir);
        assertNotNull(users.authenticate("ana@correo.com", "Nueva2#"));
        assertThrows(AuthenticationException.class, () -> users.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void plainTextStoredPasswordFailsAuthentication() throws Exception {
        Files.write(file, List.of("Eva;Ruiz;00000000T;eva@correo.com;Clave1!;600000000;1111222233334444"));
        TestContext.initialize(dir);
        assertThrows(AuthenticationException.class, () -> users.authenticate("eva@correo.com", "Clave1!"));
        assertFalse(new BCryptPasswordHasher(4).matches("x", null));
    }

    @Test
    void rejectsDuplicateDni() {
        DuplicateException e = assertThrows(DuplicateException.class, () -> users.create("Eva", "Ruiz", "12345678z",
                "eva@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertTrue(e.getMessage().contains("DNI"));
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        assertThrows(DuplicateException.class, () -> users.create("Eva", "Ruiz", "00000000T",
                "ANA@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(ValidationException.class, () -> users.create("", "Ruiz", "00000000T",
                "eva@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(ValidationException.class, () -> users.create("Eva", "Ruiz", "00000000A",
                "eva@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(ValidationException.class, () -> users.create("Eva", "Ruiz", "00000000T",
                "eva@correo.es", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(ValidationException.class, () -> users.create("Eva", "Ruiz", "00000000T",
                "eva@correo.com", "clave1!", "600000000", "1111222233334444"));
        assertThrows(ValidationException.class, () -> users.create("Eva", "Ruiz", "00000000T",
                "eva@correo.com", "Clave1!", "600000000", "1111 2222 3333 4444"));
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void deletesWithCorrectPassword() throws Exception {
        users.delete("pedro@correo.com", "Otra2#");
        assertNull(users.findByEmail("pedro@correo.com"));
        TestContext.initialize(dir);
        assertNull(users.findByEmail("pedro@correo.com"));
    }

    @Test
    void deleteRejectsWrongPasswordAndUnknownUser() {
        AuthenticationException wrong = assertThrows(AuthenticationException.class,
                () -> users.delete("ana@correo.com", "Wrong1!"));
        assertTrue(wrong.getMessage().contains("Incorrect password"));
        NotFoundException unknown = assertThrows(NotFoundException.class,
                () -> users.delete("nobody@correo.com", "Clave1!"));
        assertTrue(unknown.getMessage().contains("not found"));
        assertEquals(2, users.getUsers().size());
    }

    @Test
    void updatesFieldsAndPersists() throws Exception {
        users.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        users.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        users.update("ana.nueva@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        users.update("ana.nueva@correo.com", "Clave1!", UserField.SURNAME, "Garcia");

        TestContext.initialize(dir);
        User ana = users.findByEmail("ana.nueva@correo.com");
        assertNotNull(ana);
        assertEquals("699999999", ana.getPhone());
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updateRejectsInvalidOrTakenValues() {
        assertThrows(DuplicateException.class,
                () -> users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(ValidationException.class,
                () -> users.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(ValidationException.class,
                () -> users.update("ana@correo.com", "Clave1!", UserField.CARD, "123"));
        assertThrows(AuthenticationException.class,
                () -> users.update("ana@correo.com", "Bad1!", UserField.PHONE, "600000000"));
        User ana = users.findByEmail("ana@correo.com");
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("612345678", ana.getPhone());
    }

    @Test
    void updateKeepingOwnEmailIsAllowed() throws Exception {
        users.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void rollsBackWhenSaveFails() throws Exception {
        Files.delete(file);
        Files.createDirectory(file);
        Files.createFile(file.resolve("blocker"));
        assertThrows(PersistenceException.class,
                () -> users.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        assertEquals("612345678", users.findByEmail("ana@correo.com").getPhone());
        assertThrows(PersistenceException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        assertNotNull(users.findByEmail("ana@correo.com"));
    }
}
