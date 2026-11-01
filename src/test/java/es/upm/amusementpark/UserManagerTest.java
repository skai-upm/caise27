package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    public void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserStorage(file));
    }

    private void createAna() throws UserOperationException {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() throws UserOperationException {
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserManager restart() throws IOException {
        return new UserManager(new UserStorage(file));
    }

    @Test
    public void createValidUserIsPersisted() throws Exception {
        createAna();
        assertEquals(1, manager.getUsers().size());
        assertEquals(1, Files.readAllLines(file).size());
        assertNotNull(restart().findByEmail("ana@correo.com"));
    }

    @Test
    public void createRejectsInvalidFields() {
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678A",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234 5678 9012 3456"));
        assertThrows(UserOperationException.class, () -> manager.create("", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(manager.getUsers().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    public void createRejectsDuplicateDniAndEmail() throws Exception {
        createAna();
        String before = Files.readString(file);
        assertThrows(UserOperationException.class, () -> manager.create("Otro", "X", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(UserOperationException.class, () -> manager.create("Otro", "X", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
        assertEquals(before, Files.readString(file));
    }

    @Test
    public void deleteWithCorrectCredentials() throws Exception {
        createAna();
        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @Test
    public void deleteWithWrongCredentialsChangesNothing() throws Exception {
        createAna();
        assertThrows(UserOperationException.class, () -> manager.delete("ana@correo.com", "Wrong1!"));
        assertThrows(UserOperationException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, restart().getUsers().size());
    }

    @Test
    public void updateFieldsAndPersist() throws Exception {
        createAna();
        manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666");
        manager.update("ana.nueva@correo.com", "Nueva2#", "PHONE", "699999999");
        manager.update("ana.nueva@correo.com", "Nueva2#", "SURNAME", "Garcia");

        User u = restart().findByEmail("ana.nueva@correo.com");
        assertNotNull(u);
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia", u.getSurname());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCard());
        assertEquals("699999999", u.getPhone());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    public void updateWithWrongCredentialsChangesNothing() throws Exception {
        createAna();
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Wrong1!", "NAME", "X"));
        assertThrows(UserOperationException.class,
                () -> manager.update("nadie@correo.com", "Clave1!", "NAME", "X"));
        assertEquals("Ana", restart().findByEmail("ana@correo.com").getName());
    }

    @Test
    public void updateRejectsDniAndUnknownFields() throws Exception {
        createAna();
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "AGE", "30"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    public void updateRevalidatesNewValues() throws Exception {
        createAna();
        createPedro();
        String before = Files.readString(file);
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es"));
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(UserOperationException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "CARD", "123456789012345"));
        User ana = manager.findByEmail("ana@correo.com");
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
        assertEquals(before, Files.readString(file));
    }

    @Test
    public void updateEmailToOwnEmailIsAllowed() throws Exception {
        createAna();
        manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    public void restartKeepsIdentifiersReserved() throws Exception {
        createAna();
        UserManager second = restart();
        assertThrows(UserOperationException.class, () -> second.create("Otro", "X", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertNull(second.findByEmail("otro@correo.com"));
    }
}
