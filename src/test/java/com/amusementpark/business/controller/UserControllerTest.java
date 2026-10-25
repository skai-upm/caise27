package com.amusementpark.business.controller;

import com.amusementpark.business.model.User;
import com.amusementpark.TestSupport;
import com.amusementpark.presentation.Bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UserControllerTest {

    private static final String ANA_LINE =
            "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private Path file;
    private UserController manager;

    @BeforeEach
    public void setUp() throws Exception {
        file = dir.resolve("users.txt");
        manager = start();
    }

    private UserController start() throws Exception {
        Bootstrap.initialize(dir, LocalDate.of(2026, 10, 1), TestSupport.FAST_HASHER, new PrintStream(OutputStream.nullOutputStream()));
        return UserController.getInstance();
    }

    @Test
    public void singleInstance() {
        assertSame(UserController.getInstance(), UserController.getInstance());
    }

    @Test
    public void reinitialisingWithAnotherDirectoryResetsState() throws Exception {
        createAna();
        Path other = Files.createDirectory(dir.resolve("other"));
        Bootstrap.initialize(other,
                LocalDate.of(2026, 10, 1), TestSupport.FAST_HASHER, new PrintStream(OutputStream.nullOutputStream()));
        assertTrue(UserController.getInstance().getUsers().isEmpty());
        assertSame(manager, UserController.getInstance());
    }

    @Test
    public void passwordsAreHashedAndVerified() throws Exception {
        createAna();
        String stored = only().getPassword();
        assertTrue(TestSupport.FAST_HASHER.isHash(stored));
        assertTrue(manager.authenticate("ana@correo.com", "Clave1!") != null);
        assertTrue(manager.authenticate("ana@correo.com", stored) == null);
        assertTrue(manager.update("ana@correo.com", "Clave1!", "PASSWORD", "CLAVE1!").startsWith("ERROR"));
        assertEquals(stored, only().getPassword());
    }

    @Test
    public void legacyPlainTextPasswordsAreMigratedOnLoad() throws Exception {
        Files.write(file, List.of(ANA_LINE));
        manager = start();
        TestSupport.assertStoredUsers(List.of(ANA_LINE), Files.readAllLines(file));
        assertTrue(manager.delete("ana@correo.com", "Clave1!").startsWith("OK"));
    }

    private String createAna() {
        return manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    private User only() {
        assertEquals(1, manager.getUsers().size());
        return manager.getUsers().get(0);
    }

    @Test
    public void createStoresAndPersists() throws Exception {
        assertTrue(createAna().startsWith("OK"));
        TestSupport.assertStoredUsers(List.of(ANA_LINE), Files.readAllLines(file));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "12345678A|ana@correo.com|Clave1!|1234567890123456",
        "12345678Z|ana@correo.es|Clave1!|1234567890123456",
        "12345678Z|ana@correo.com|clave1!|1234567890123456",
        "12345678Z|ana@correo.com|Clave1!|123456789012345"
    })
    public void createRejectsInvalidData(String dni, String email, String password, String card) {
        String result = manager.create("Ana", "Garcia Lopez", dni, email, password, "612345678", card);
        assertTrue(result.startsWith("ERROR"), result);
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    public void createRejectsEmptyRequiredFields() {
        assertTrue(manager.create("", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456").startsWith("ERROR"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    public void createRejectsDuplicateDni() {
        createAna();
        String result = manager.create("Otra", "Persona", "12345678Z", "otro@correo.com",
                "Clave1!", "600000000", "1234567890123456");
        assertTrue(result.startsWith("ERROR"), result);
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    public void createRejectsDuplicateEmail() {
        createAna();
        String result = manager.create("Pedro", "Lopez", "87654321X", "ana@correo.com",
                "Otra2#", "623456789", "9876543210987654");
        assertTrue(result.startsWith("ERROR"), result);
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    public void deleteRemovesUserAndUpdatesFile() throws Exception {
        createAna();
        assertTrue(manager.delete("ana@correo.com", "Clave1!").startsWith("OK"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.readAllLines(file).isEmpty());
    }

    @Test
    public void deleteRejectsUnknownEmailOrWrongPassword() {
        createAna();
        assertTrue(manager.delete("nadie@correo.com", "Clave1!").startsWith("ERROR"));
        assertTrue(manager.delete("ana@correo.com", "Mala1!").startsWith("ERROR"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    public void updateSequenceFromSpecification() throws Exception {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria").startsWith("OK"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com").startsWith("OK"));
        assertTrue(manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#").startsWith("OK"));
        assertTrue(manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666").startsWith("OK"));
        assertTrue(manager.update("ana.nueva@correo.com", "Nueva2#", "SURNAME", "Garcia").startsWith("OK"));
        assertTrue(manager.update("ana.nueva@correo.com", "Nueva2#", "PHONE", "699999999").startsWith("OK"));
        TestSupport.assertStoredUsers(
                List.of("Ana Maria;Garcia;12345678Z;ana.nueva@correo.com;Nueva2#;699999999;9999888877776666"),
                Files.readAllLines(file));
        assertTrue(manager.update("ana.nueva@correo.com", "Clave1!", "NAME", "X").startsWith("ERROR"));
    }

    @Test
    public void updateRejectsWrongCredentials() {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Mala1!", "NAME", "X").startsWith("ERROR"));
        assertTrue(manager.update("x@correo.com", "Clave1!", "NAME", "X").startsWith("ERROR"));
        assertEquals("Ana", only().getName());
    }

    @Test
    public void updateRejectsDniAndUnknownFields() {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "FOO", "x").startsWith("ERROR"));
        assertEquals("12345678Z", only().getDni());
    }

    @Test
    public void updateRevalidatesNewValues() throws Exception {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "CARD", "123456789012345").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo").startsWith("ERROR"));
        TestSupport.assertStoredUsers(List.of(ANA_LINE), Files.readAllLines(file));
    }

    @Test
    public void updateRejectsEmailOfAnotherUser() {
        createAna();
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com").startsWith("OK"));
    }

    @Test
    public void usersAreReloadedByANewInstance() throws Exception {
        createAna();
        UserController reloaded = start();
        assertEquals(1, reloaded.getUsers().size());
        assertTrue(reloaded.create("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "600000000",
                "1234567890123456").startsWith("ERROR"));
        assertTrue(reloaded.delete("ana@correo.com", "Clave1!").startsWith("OK"));
    }
}
