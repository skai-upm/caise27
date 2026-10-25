package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserManagerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
        assertOk(manager.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456"));
    }

    private static void assertOk(String result) {
        assertTrue(result.startsWith("OK"), result);
    }

    private static void assertError(String result) {
        assertTrue(result.startsWith("ERROR"), result);
    }

    @Test
    void createStoresUserInFile() throws IOException {
        assertEquals(1, manager.getUsers().size());
        assertEquals(1, Files.readAllLines(file).size());
    }

    @Test
    void createRejectsInvalidDataWithoutStoring() {
        assertError(manager.createUser("Pedro", "Lopez", "12345678A", "pedro@correo.com", "Otra2#", "6",
                "9876543210987654"));
        assertError(manager.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.es", "Otra2#", "6",
                "9876543210987654"));
        assertError(manager.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "otra2#", "6",
                "9876543210987654"));
        assertError(manager.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6",
                "987654321098765"));
        assertError(manager.createUser("", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6",
                "9876543210987654"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniOrEmail() {
        assertError(manager.createUser("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "6",
                "1234567890123456"));
        assertError(manager.createUser("Otra", "Persona", "87654321X", "ana@correo.com", "Clave1!", "6",
                "1234567890123456"));
        assertError(manager.createUser("Otra", "Persona", "87654321X", "ANA@correo.com", "Clave1!", "6",
                "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void authenticateRequiresMatchingPassword() {
        assertNull(manager.authenticate("ana@correo.com", "Wrong1!"));
        assertNull(manager.authenticate("nadie@correo.com", "Clave1!"));
        assertNotNull(manager.authenticate("ANA@correo.com", "Clave1!"));
    }

    @Test
    void removeUpdatesStorage() throws IOException {
        assertTrue(manager.remove(manager.findByEmail("ana@correo.com")));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.readAllLines(file).isEmpty());
    }

    @Test
    void updateChangesEachEditableField() {
        assertOk(manager.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria"));
        assertOk(manager.updateUser("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia"));
        assertOk(manager.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        assertOk(manager.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com"));
        assertOk(manager.updateUser("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#"));
        assertOk(manager.updateUser("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666"));

        User ana = manager.findByEmail("ana.nueva@correo.com");
        assertEquals("Ana Maria", ana.getName());
        assertEquals("Garcia", ana.getSurname());
        assertEquals("699999999", ana.getPhone());
        assertEquals("Nueva2#", ana.getPassword());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
        assertNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void updateRequiresCredentialsAndValidValues() {
        assertOk(manager.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
                "623456789", "9876543210987654"));
        assertError(manager.updateUser("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        assertError(manager.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertError(manager.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertError(manager.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave"));
        assertError(manager.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        assertError(manager.updateUser("ana@correo.com", "Clave1!", UserField.NAME, " "));

        User ana = manager.findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void stateSurvivesRestart() throws IOException {
        assertOk(manager.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        UserManager restarted = new UserManager(new UserRepository(file));
        User ana = restarted.findByEmail("ana@correo.com");
        assertNotNull(ana);
        assertEquals("699999999", ana.getPhone());
        assertNotNull(restarted.authenticate("ana@correo.com", "Clave1!"));
    }
}
