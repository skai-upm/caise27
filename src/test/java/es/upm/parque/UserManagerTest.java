package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserManagerTest {

    @TempDir
    Path tempDir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    public void setUp() {
        file = tempDir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
    }

    private void createAna() {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() {
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private UserManager restart() {
        return new UserManager(new UserRepository(file));
    }

    @Test
    public void createsValidUserAndPersistsIt() {
        createAna();
        assertEquals(1, manager.getUsers().size());
        User ana = restart().findByEmail("ana@correo.com");
        assertNotNull(ana);
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    public void rejectsInvalidFieldsOnCreate() {
        assertThrows(UserException.class, () -> manager.create("", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Ana", " ", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Ana", "Garcia", "12345678A", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Ana", "Garcia", "12345678Z", "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "123456789012345"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @Test
    public void rejectsDuplicateDniOrEmail() {
        createAna();
        assertThrows(UserException.class, () -> manager.create("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Otra", "Persona", "87654321X", "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(UserException.class, () -> manager.create("Otra", "Persona", "87654321X", "ANA@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    public void uniquenessHoldsAfterRestart() {
        createAna();
        UserManager reloaded = restart();
        assertThrows(UserException.class, () -> reloaded.create("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
    }

    @Test
    public void lowercaseDniLetterIsNormalized() {
        manager.create("Ana", "Garcia Lopez", "12345678z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    public void deletesWithCorrectCredentials() {
        createAna();
        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @Test
    public void doesNotDeleteWithWrongPasswordOrUnknownEmail() {
        createAna();
        assertThrows(UserException.class, () -> manager.delete("ana@correo.com", "Otra1!"));
        assertThrows(UserException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, restart().getUsers().size());
    }

    @Test
    public void updatesEveryEditableField() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");

        User ana = restart().findByDni("12345678Z");
        assertEquals("Ana Maria", ana.getName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("ana.nueva@correo.com", ana.getEmail());
        assertTrue(ana.passwordMatches("Nueva2#"));
        assertEquals("9999888877776666", ana.getCard());
        assertNull(restart().findByEmail("ana@correo.com"));
    }

    @Test
    public void updateRevalidatesNewValues() {
        createAna();
        createPedro();
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "123"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.NAME, ""));
        User ana = restart().findByDni("12345678Z");
        assertEquals("ana@correo.com", ana.getEmail());
        assertTrue(ana.passwordMatches("Clave1!"));
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Ana", ana.getName());
    }

    @Test
    public void updateKeepingSameEmailIsAllowed() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    public void updateRequiresValidCredentials() {
        createAna();
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertThrows(UserException.class, () -> manager.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertEquals("Ana", manager.findByEmail("ana@correo.com").getName());
    }
}
