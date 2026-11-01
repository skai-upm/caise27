package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserManagerTest {

    @TempDir
    Path tempDir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = tempDir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
    }

    private void createAna() {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() {
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserManager restart() throws IOException {
        return new UserManager(new UserRepository(file));
    }

    @Test
    void createsAndPersistsValidUser() throws IOException {
        createAna();
        assertEquals(1, manager.getUsers().size());
        assertNotNull(restart().findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsInvalidDataWithoutStoring() {
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678A",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234 5678 9012 3456"));
        assertThrows(UserOperationException.class, () -> manager.create(" ", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    void rejectsDuplicateDniOrEmail() {
        createAna();
        assertThrows(UserOperationException.class, () -> manager.create("Otro", "X", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Otro", "X", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deletesWithCorrectCredentialsAndPersists() throws IOException {
        createAna();
        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @Test
    void deleteWithWrongCredentialsChangesNothing() throws IOException {
        createAna();
        List<String> before = Files.readAllLines(file);
        assertThrows(UserOperationException.class, () -> manager.delete("ana@correo.com", "Wrong1!"));
        assertThrows(UserOperationException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, manager.getUsers().size());
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void chainedUpdatesUseNewCredentialsAndPersist() throws IOException {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.PHONE, "699999999");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.SURNAME, "Garcia");

        User user = restart().findByEmail("ana.nueva@correo.com");
        assertNotNull(user);
        assertEquals("Ana Maria", user.getName());
        assertEquals("Garcia", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("Nueva2#", user.getPassword());
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
    }

    @Test
    void updateWithWrongCredentialsChangesNothing() {
        createAna();
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        assertThrows(UserOperationException.class,
                () -> manager.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertEquals("Ana", manager.findByEmail("ana@correo.com").getName());
    }

    @Test
    void rejectsEmailOwnedByAnotherUser() {
        createAna();
        createPedro();
        assertThrows(UserOperationException.class,
                () -> manager.update("pedro@correo.com", "Otra2#", UserField.EMAIL, "ana@correo.com"));
        assertNotNull(manager.findByEmail("pedro@correo.com"));
    }

    @Test
    void allowsKeepingOwnEmail() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void invalidNewValuesLeaveStateIntact() throws IOException {
        createAna();
        List<String> before = Files.readAllLines(file);
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "123"));
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        User user = manager.findByEmail("ana@correo.com");
        assertEquals("Clave1!", user.getPassword());
        assertEquals("1234567890123456", user.getCard());
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void failedSaveRevertsInMemoryChange() throws IOException {
        Path unwritable = tempDir.resolve("missing-dir").resolve("users.txt");
        UserManager blocked = new UserManager(new UserRepository(unwritable));
        assertThrows(UserOperationException.class, () -> blocked.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(blocked.getUsers().isEmpty());
        assertNull(blocked.findByEmail("ana@correo.com"));
    }
}
