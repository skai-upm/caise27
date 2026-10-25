package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserControllerTest {

    @TempDir
    Path dir;

    private TestContext ctx;
    private UserController users;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        users = ctx.users();
        ok(users.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456"));
    }

    private Path file() {
        return dir.resolve("users.txt");
    }

    @Test
    void createStoresUserWithHashedPassword() throws IOException {
        assertEquals(1, users.getUsers().size());
        List<String> lines = Files.readAllLines(file());
        assertEquals(1, lines.size());
        assertFalse(lines.get(0).contains("Clave1!"));
        String[] fields = lines.get(0).split(";");
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com", String.join(";", List.of(fields).subList(0, 4)));
        assertTrue(TestContext.FAST_ENCODER.isHash(fields[4]));
        assertTrue(TestContext.FAST_ENCODER.matches("Clave1!", fields[4]));
        assertEquals("612345678;1234567890123456", fields[5] + ";" + fields[6]);
    }

    @Test
    void createRejectsInvalidDataWithoutStoring() {
        error(users.createUser("Pedro", "Lopez", "12345678A", "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
        error(users.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.es", "Otra2#", "6", "9876543210987654"));
        error(users.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "otra2#", "6", "9876543210987654"));
        error(users.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "987654321098765"));
        error(users.createUser("", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, users.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniOrEmail() {
        error(users.createUser("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456"));
        error(users.createUser("Otra", "Persona", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        error(users.createUser("Otra", "Persona", "87654321X", "ANA@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(1, users.getUsers().size());
    }

    @Test
    void authenticationUsesTheStoredHash() {
        assertNull(users.authenticate("ana@correo.com", "Wrong1!"));
        assertNull(users.authenticate("nadie@correo.com", "Clave1!"));
        assertNotNull(users.authenticate("ANA@correo.com", "Clave1!"));
        error(users.deleteUser("ana@correo.com", "Wrong1!"));
        error(users.updateUser("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        ok(users.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(users.getUsers().isEmpty());
    }

    @Test
    void updateChangesEachEditableField() {
        ok(users.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria"));
        ok(users.updateUser("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia"));
        ok(users.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        ok(users.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com"));
        ok(users.updateUser("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#"));
        ok(users.updateUser("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666"));

        User ana = users.findByEmail("ana.nueva@correo.com");
        assertEquals("Ana Maria", ana.getName());
        assertEquals("Garcia", ana.getSurname());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
        assertNull(users.findByEmail("ana@correo.com"));
        assertNull(users.authenticate("ana.nueva@correo.com", "Clave1!"));
        assertNotNull(users.authenticate("ana.nueva@correo.com", "Nueva2#"));
    }

    @Test
    void updateRequiresValidValues() {
        ok(users.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654"));
        String hashBefore = users.findByEmail("ana@correo.com").getPasswordHash();
        error(users.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        error(users.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        error(users.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave"));
        error(users.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        error(users.updateUser("ana@correo.com", "Clave1!", UserField.NAME, " "));

        User ana = users.findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertEquals(hashBefore, ana.getPasswordHash());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void passwordChangePersistsANewHash() throws IOException {
        String hashBefore = Files.readAllLines(file()).get(0).split(";")[4];
        ok(users.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#"));
        String hashAfter = Files.readAllLines(file()).get(0).split(";")[4];
        assertNotEquals(hashBefore, hashAfter);
        assertNotNull(ctx.restart().users().authenticate("ana@correo.com", "Nueva2#"));
    }

    @Test
    void legacyPlaintextPasswordsAreHashedOnLoad() throws IOException {
        Files.writeString(file(), "Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456\n");
        TestContext restarted = ctx.restart();
        assertFalse(Files.readString(file()).contains("Clave1!"));
        assertNotNull(restarted.users().authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void stateSurvivesRestart() throws IOException {
        ok(users.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        UserController restarted = ctx.restart().users();
        User ana = restarted.findByEmail("ana@correo.com");
        assertNotNull(ana);
        assertEquals("699999999", ana.getPhone());
        assertNotNull(restarted.authenticate("ana@correo.com", "Clave1!"));
    }
}
